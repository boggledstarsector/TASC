package data.scripts;

import com.fs.starfarer.api.campaign.econ.MarketAPI;

public class BoggledTerraformingDurationModifier {
    public interface TerraformingDurationModifier {
        float getDurationModifier(BoggledTerraformingRequirement.RequirementContext ctx, int baseDuration);
    }

    public static class PlanetSize implements TerraformingDurationModifier {
        @Override
        public float getDurationModifier(BoggledTerraformingRequirement.RequirementContext ctx, int baseDuration) {
            return ctx.getPlanet().getRadius();
        }
    }
}
