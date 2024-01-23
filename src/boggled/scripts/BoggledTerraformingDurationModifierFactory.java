package boggled.scripts;

public class BoggledTerraformingDurationModifierFactory {
    public interface TerraformingDurationModifierFactory {
        BoggledTerraformingDurationModifier.TerraformingDurationModifier constructFromJSON(String id, String[] enableSettings, String data);
    }

    public static class PlanetSize implements TerraformingDurationModifierFactory {
        @Override
        public BoggledTerraformingDurationModifier.TerraformingDurationModifier constructFromJSON(String id, String[] enableSettings, String data) {
            return new BoggledTerraformingDurationModifier.PlanetSize(id, enableSettings);
        }
    }

    public static class Setting implements TerraformingDurationModifierFactory {
        @Override
        public BoggledTerraformingDurationModifier.TerraformingDurationModifier constructFromJSON(String id, String[] enableSettings, String data) {
            return new BoggledTerraformingDurationModifier.Setting(id, enableSettings, data);
        }
    }
}
