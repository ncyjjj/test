package com.materialmaker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.simulation")
public class SimulationProperties {
    private boolean enabled = true;
    private DelayMs delayMs = new DelayMs();
    private String failAtStage = "";

    public static class DelayMs {
        private long assetCheck = 800;
        private long generate = 1500;
        private long finalize = 600;

        public long getAssetCheck() { return assetCheck; }
        public void setAssetCheck(long assetCheck) { this.assetCheck = assetCheck; }
        public long getGenerate() { return generate; }
        public void setGenerate(long generate) { this.generate = generate; }
        public long getFinalize() { return finalize; }
        public void setFinalize(long finalize) { this.finalize = finalize; }
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public DelayMs getDelayMs() { return delayMs; }
    public void setDelayMs(DelayMs delayMs) { this.delayMs = delayMs; }
    public String getFailAtStage() { return failAtStage; }
    public void setFailAtStage(String failAtStage) { this.failAtStage = failAtStage; }
}
