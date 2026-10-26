package me.alpha432.oyvey.manager;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.event.impl.render.Render2DEvent;
import me.alpha432.oyvey.event.impl.render.Render3DEvent;
import me.alpha432.oyvey.features.Feature;
import me.alpha432.oyvey.features.commands.ModuleCommand;
import me.alpha432.oyvey.features.gui.OyVeyGui;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.modules.Register;
import me.alpha432.oyvey.util.traits.Jsonable;
import me.alpha432.oyvey.util.traits.Util;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public class ModuleManager implements Jsonable, Util {
    private static final Logger LOGGER = LoggerFactory.getLogger("ModuleManager");
    private static final String MODULE_PACKAGE = "me.alpha432.oyvey.features.modules";

    private final Map<Class<? extends Module>, Module> fastRegistry = new HashMap<>();
    private final List<Module> modules = new ArrayList<>();

    public void init() {
        for (Module module : discoverModules()) {
            register(module);
        }

        LOGGER.info("Registered {} modules", modules.size());

        // Create a command for each module for modules to be configurable via command line
        for (Module module : modules) {
            OyVey.commandManager.register(new ModuleCommand(module));
        }

        OyVey.configManager.addConfig(this);
    }

    public void register(Module module) {
        if (module.getName() == null || module.getName().isEmpty()) {
            module.setName(nameFor(module.getClass()));
        }

        getModules().add(module);
        fastRegistry.put(module.getClass(), module);
    }

    private List<Module> discoverModules() {
        Set<Class<? extends Module>> types = new TreeSet<>(Comparator.comparing(Class::getName));
        scanClassLoader(types);

        if (types.isEmpty()) {
            scanModContainer(types);
        }

        List<Module> discovered = new ArrayList<>();

        for (Class<? extends Module> type : types) {
            try {
                Module module = type.getDeclaredConstructor().newInstance();
                if (module.getName() == null || module.getName().isEmpty()) {
                    module.setName(nameFor(type));
                }
                discovered.add(module);
            } catch (ReflectiveOperationException e) {
                LOGGER.error("Failed to instantiate module {}", type.getName(), e);
            }
        }

        discovered.sort(Comparator
                .comparingInt((Module module) -> module.getCategory().ordinal())
                .thenComparing(Module::getName, String.CASE_INSENSITIVE_ORDER));

        return discovered;
    }

    private void scanClassLoader(Set<Class<? extends Module>> types) {
        String packagePath = MODULE_PACKAGE.replace('.', '/');

        try {
            Enumeration<URL> resources = ModuleManager.class.getClassLoader().getResources(packagePath);

            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                switch (url.getProtocol()) {
                    case "file" -> scanDirectory(Paths.get(url.toURI()), types);
                    case "jar" -> scanJar(url, packagePath, types);
                    default -> {
                    }
                }
            }
        } catch (IOException | URISyntaxException e) {
            LOGGER.error("Failed to scan module classes", e);
        }
    }

    private void scanModContainer(Set<Class<? extends Module>> types) {
        FabricLoader.getInstance().getModContainer("oyvey")
                .flatMap(container -> container.findPath(MODULE_PACKAGE.replace('.', '/')))
                .ifPresent(root -> scanDirectory(root, types));
    }

    private void scanDirectory(Path root, Set<Class<? extends Module>> types) {
        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(Files::isRegularFile)
                    .map(root::relativize)
                    .map(Path::toString)
                    .filter(name -> name.endsWith(".class"))
                    .filter(name -> !name.contains("$"))
                    .forEach(name -> {
                        String className = name.substring(0, name.length() - ".class".length())
                                .replace(File.separatorChar, '.')
                                .replace('/', '.');
                        addModuleClass(MODULE_PACKAGE + "." + className, types);
                    });
        } catch (IOException e) {
            LOGGER.error("Failed to scan module directory {}", root, e);
        }
    }

    private void scanJar(URL url, String packagePath, Set<Class<? extends Module>> types) {
        try {
            JarURLConnection connection = (JarURLConnection) url.openConnection();
            connection.setUseCaches(false);

            try (JarFile jar = connection.getJarFile()) {
                String prefix = packagePath + "/";

                jar.stream()
                        .filter(entry -> !entry.isDirectory())
                        .map(JarEntry::getName)
                        .filter(name -> name.startsWith(prefix))
                        .filter(name -> name.endsWith(".class"))
                        .filter(name -> !name.contains("$"))
                        .forEach(name -> addModuleClass(
                                name.substring(0, name.length() - ".class".length()).replace('/', '.'),
                                types));
            }
        } catch (IOException e) {
            LOGGER.error("Failed to scan module jar {}", url, e);
        }
    }

    @SuppressWarnings("unchecked")
    private void addModuleClass(String className, Set<Class<? extends Module>> types) {
        try {
            Class<?> type = Class.forName(className, false, ModuleManager.class.getClassLoader());

            if (!Module.class.isAssignableFrom(type)
                    || java.lang.reflect.Modifier.isAbstract(type.getModifiers())
                    || !type.isAnnotationPresent(Register.class)) {
                return;
            }

            types.add((Class<? extends Module>) type);
        } catch (ClassNotFoundException | LinkageError e) {
            LOGGER.warn("Skipping unloadable module class {}", className, e);
        }
    }

    private static String nameFor(Class<? extends Module> type) {
        Register register = type.getAnnotation(Register.class);

        if (register != null && !register.value().isBlank()) {
            return register.value();
        }

        String name = type.getSimpleName();
        return name.endsWith("Module")
                ? name.substring(0, name.length() - "Module".length())
                : name;
    }

    public List<Module> getModules() {
        return modules;
    }

    public Stream<Module> stream() {
        return getModules().stream();
    }

    @SuppressWarnings("unchecked")
    public <T extends Module> T getModuleByClass(Class<T> clazz) {
        return (T) fastRegistry.get(clazz);
    }

    public Module getModuleByName(String name) {
        return stream().filter(m -> m.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public Module getModuleByDisplayName(String display) {
        return stream().filter(m -> m.getDisplayName().equalsIgnoreCase(display)).findFirst().orElse(null);
    }

    public List<Module> getModulesByCategory(Module.Category category) {
        return stream().filter(m -> m.getCategory() == category).toList();
    }

    public List<Module.Category> getCategories() {
        return Arrays.asList(Module.Category.values());
    }

    public void onLoad() {
        getModules().forEach(Module::onLoad);
    }

    public void onTick() {
        stream().filter(Feature::isEnabled).forEach(Module::onTick);
    }

    public void onRender2D(Render2DEvent event) {
        stream().filter(Feature::isEnabled).forEach(module -> module.onRender2D(event));
    }

    public void onRender3D(Render3DEvent event) {
        stream().filter(Feature::isEnabled).forEach(module -> module.onRender3D(event));
    }

    public void onUnload() {
        getModules().forEach(EVENT_BUS::unregister);
        getModules().forEach(Module::onUnload);
    }

    public void onKeyPressed(int key) {
        if (key <= 0) return;
        if (mc.gui.screen() instanceof TitleScreen || mc.gui.screen() instanceof JoinMultiplayerScreen) {
            if (me.alpha432.oyvey.features.modules.client.ClickGuiModule.getInstance().getBind().getKey() == key) {
                OyVeyGui.open(mc.gui.screen());
            }
            return;
        }
        if (mc.gui.screen() != null) return;
        stream().filter(module -> module.getBind().getKey() == key).forEach(Module::toggle);
    }

    public void onMouseClicked(int button) {
        if (mc.gui.screen() != null) return;
        int key = -button - 2;
        stream().filter(module -> module.getBind().getKey() == key).forEach(Module::toggle);
    }

    @Override
    public JsonElement toJson() {
        JsonObject object = new JsonObject();
        for (Module module : getModules()) {
            object.add(module.getName(), module.toJson());
        }
        return object;
    }

    @Override
    public void fromJson(JsonElement element) {
        for (Module module : getModules()) {
            module.fromJson(element.getAsJsonObject().get(module.getName()));
        }
    }

    @Override
    public String getFileName() {
        return "modules.json";
    }
}
