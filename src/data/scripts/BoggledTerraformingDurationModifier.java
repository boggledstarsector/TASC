package data.scripts;

import com.fs.starfarer.api.campaign.econ.MarketAPI;

public class BoggledTerraformingDurationModifier {
    public interface TerraformingDurationModifier {
        float getDurationModifier(MarketAPI market, int baseDuration);
    }

    public static class PlanetSize implements TerraformingDurationModifier {
        @Override
        public float getDurationModifier(MarketAPI market, int baseDuration) {
            return market.getPlanetEntity().getRadius();
        }
    }
}
