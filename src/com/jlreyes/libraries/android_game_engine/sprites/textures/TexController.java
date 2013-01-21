package com.jlreyes.libraries.android_game_engine.sprites.textures;

/**
 * Class holding texture info.
 */
public class TexController {
    public static class TexInfo {
        public String Name;
        public long Version;
        public int NumFrames;
        /* Texture States Info */
        public TexStateInfo[] States;
        public TexStateInfo DefaultState;
        /* Frame Width and Height */
        public int FrameWidthHigh;
        public int FrameHeightHigh;
        public int FrameWidthMed;
        public int FrameHeightMed;
        public int FrameWidthLow;
        public int FrameHeightLow;

        /**
         * Create a new Texture Info object with the given
         * name, version, and width/height
         *
         * @param name    The name identifying this TexInfo
         * @param version The version of this TexInfo. Will be stored on
         *                the device- a change in the version number will result
         *                in a reloading of this texture
         * @param highRes An array of length 2 where index 0 is the resolution
         *                width and index 1 is the resolution height.
         * @param medRes  An array of length 2 where index 0 is the resolution
         *                width and index 1 is the resolution height.
         * @param lowRes  An Array of length 2 where index 0 is the resolution
         *                width and index 1 is the resolution height.
         * @param states  An array of texture states this texture can be in.
         *                If index 0 exists, we set this as the default state.
         */
        public TexInfo(String name, long version,
                       int[] highRes, int[] medRes, int[] lowRes,
                       TexStateInfo[] states) {
            this.Name = name;
            this.Version = version;
            this.setHighRes(highRes[0], highRes[1]);
            this.setMedRes(medRes[0], medRes[1]);
            this.setLowRes(lowRes[0], lowRes[1]);
            this.States = states;
            if (this.States.length > 0) this.DefaultState = this.States[0];
            else this.States = null;
            this.NumFrames = 0;
            for (int i = 0; i < this.States.length; i++)
                this.NumFrames += this.States[i].NumFrames;
        }

        /**
         * Passes in an empty array for the states argument.
         */
        public TexInfo(String name, long version,
                       int[] highRes, int[] medRes, int[] lowRes) {
            this(name, version, highRes, medRes, lowRes, new TexStateInfo[]{});
        }

        public void addTexState(TexStateInfo stateInfo) {
            TexStateInfo[] oldStates = this.States;
            this.States = new TexStateInfo[oldStates.length + 1];
            System.arraycopy(oldStates, 0, this.States, 0, oldStates.length);
            this.States[oldStates.length] = stateInfo;

            this.NumFrames += stateInfo.NumFrames;
            if (this.DefaultState == null) this.DefaultState = stateInfo;
        }

        public int FrameWidth() {
            switch (TexController.RESOLUTION) {
                case HIGH:
                    return this.FrameWidthHigh;
                case MED:
                    return this.FrameWidthMed;
                case LOW:
                    return this.FrameWidthLow;
                default:
                    throw new IllegalStateException();
            }
        }

        public int FrameHeight() {
            switch (TexController.RESOLUTION) {
                case HIGH:
                    return this.FrameHeightHigh;
                case MED:
                    return this.FrameHeightMed;
                case LOW:
                    return this.FrameHeightLow;
                default:
                    throw new IllegalStateException();
            }
        }

        private void setHighRes(int width, int height) {
            this.FrameWidthHigh = width;
            this.FrameHeightHigh = height;
        }

        private void setMedRes(int width, int height) {
            this.FrameWidthMed = width;
            this.FrameHeightMed = height;
        }

        private void setLowRes(int width, int height) {
            this.FrameWidthLow = width;
            this.FrameHeightLow = height;
        }

        public TexStateInfo getTexState(String name) {
            int len = this.States.length;
            for (int i = 0; i < len; i++)
                if (this.States[i].Name.equals(name)) return this.States[i];
            throw new IllegalStateException("Texture state " + name +
                                            "not found for " + this);
        }

        @Override
        public String toString() {
            return this.Name + " - v" + this.Version + ": " + super.toString();
        }
    }

    /**
     * Represents a state that a texture can be in
     */
    public static class TexStateInfo {
        public String Name;
        public int[] FrameTimes;
        public int NumFrames;
        public int HighResourceId;
        public int MedResourceId;
        public int LowResourceId;

        /**
         * Create a new TexStateInfo instance.
         */
        public TexStateInfo(String name,
                            int[] frameTimes,
                            int highResourceId,
                            int medResourceId,
                            int lowResourceId) {
            this.Name = name;
            this.FrameTimes = frameTimes;
            this.NumFrames = frameTimes.length;
            this.HighResourceId = highResourceId;
            this.MedResourceId = medResourceId;
            this.LowResourceId = lowResourceId;
        }

        public int ResourceId() {
            switch (TexController.RESOLUTION) {
                case HIGH:
                    return this.HighResourceId;
                case MED:
                    return this.MedResourceId;
                case LOW:
                    return this.LowResourceId;
                default:
                    throw new IllegalStateException();
            }
        }
    }

    public static enum Resolution {
        HIGH, MED, LOW
    }

    public static final TexInfo NO_TEX = null;
    public static TexInfo[] TEXTURES;
    public static Resolution RESOLUTION = Resolution.HIGH;
}

