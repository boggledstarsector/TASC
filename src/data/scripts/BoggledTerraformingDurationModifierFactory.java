package data.scripts;

public class BoggledTerraformingDurationModifierFactory {
    public interface TerraformingDurationModifierFactory {
        BoggledTerraformingDurationModifier.TerraformingDurationModifier constructFromJSON(String data);
    }

    public static class PlanetSize implements TerraformingDurationModifierFactory {
        @Override
        public BoggledTerraformingDurationModifier.TerraformingDurationModifier constructFromJSON(String data) {
            return new BoggledTerraformingDurationModifier.PlanetSize();
        }
    }
}
