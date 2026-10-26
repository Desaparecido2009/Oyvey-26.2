package me.alpha432.oyvey.util.models;

public final class Animation {
    public enum Easing {
        LINEAR {
            @Override
            public float apply(float t) {
                return t;
            }
        },
        OUT_QUAD {
            @Override
            public float apply(float t) {
                return 1.0f - (1.0f - t) * (1.0f - t);
            }
        },
        IN_QUAD {
            @Override
            public float apply(float t) {
                return t * t;
            }
        },
        OUT_CUBIC {
            @Override
            public float apply(float t) {
                float inv = 1.0f - t;
                return 1.0f - inv * inv * inv;
            }
        },
        IN_CUBIC {
            @Override
            public float apply(float t) {
                return t * t * t;
            }
        };

        public abstract float apply(float t);
    }

    private float value;
    private float startValue;
    private float target;
    private long startTime;
    private long durationNanos;
    private Easing easing = Easing.LINEAR;

    public Animation() {
    }

    public Animation(float value) {
        set(value);
    }

    public void set(float value) {
        this.value = value;
        this.startValue = value;
        this.target = value;
        this.durationNanos = 0L;
    }

    public void animateTo(float target, long durationMs, Easing easing) {
        if (durationMs <= 0L) {
            set(target);
            return;
        }
        this.startValue = this.value;
        this.target = target;
        this.startTime = System.nanoTime();
        this.durationNanos = durationMs * 1_000_000L;
        this.easing = easing;
    }

    public float get() {
        if (this.durationNanos <= 0L) {
            return this.value;
        }
        long elapsed = System.nanoTime() - this.startTime;
        if (elapsed >= this.durationNanos) {
            this.value = this.target;
            this.durationNanos = 0L;
            return this.value;
        }
        float progress = (float) elapsed / (float) this.durationNanos;
        this.value = this.startValue + (this.target - this.startValue) * this.easing.apply(progress);
        return this.value;
    }

    public float getValue() {
        return this.value;
    }

    public boolean isFinished() {
        return this.durationNanos <= 0L;
    }
}
