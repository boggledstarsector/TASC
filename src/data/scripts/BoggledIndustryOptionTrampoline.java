package data.scripts;

import data.campaign.econ.industries.*;
import org.json.JSONException;
import org.json.JSONObject;

public class BoggledIndustryOptionTrampoline {
    public interface IndustryOptionTrampoline {
        void initialiseOptionsFromJSON(JSONObject data) throws JSONException;
    }

    public static class AIMiningDrones implements IndustryOptionTrampoline {
        @Override
        public void initialiseOptionsFromJSON(JSONObject data) throws JSONException {
            Boggled_AI_Mining_Drones.settingsFromJSON(data);
        }
    }

    public static class AtmosphereProcessor implements IndustryOptionTrampoline {
        @Override
        public void initialiseOptionsFromJSON(JSONObject data) throws JSONException {
            Boggled_Atmosphere_Processor.settingsFromJSON(data);
        }
    }

    public static class CHAMELEON implements IndustryOptionTrampoline {
        @Override
        public void initialiseOptionsFromJSON(JSONObject data) throws JSONException {
            Boggled_CHAMELEON.settingsFromJSON(data);
        }
    }

    public static class Cloning implements IndustryOptionTrampoline {
        @Override
        public void initialiseOptionsFromJSON(JSONObject data) throws JSONException {
            Boggled_Cloning.settingsFromJSON(data);
        }
    }

    public static class Cryosanctum implements IndustryOptionTrampoline {
        @Override
        public void initialiseOptionsFromJSON(JSONObject data) throws JSONException {
            Boggled_Cryosanctum.settingsFromJSON(data);
        }
    }

    public static class DomainArchaeology implements IndustryOptionTrampoline {
        @Override
        public void initialiseOptionsFromJSON(JSONObject data) throws JSONException {
            Boggled_Domain_Archaeology.settingsFromJSON(data);
        }
    }

    public static class DomedCitiesIndustryTrampoline implements IndustryOptionTrampoline {
        @Override
        public void initialiseOptionsFromJSON(JSONObject data) throws JSONException {
            Boggled_Domed_Cities.settingsFromJSON(data);
        }
    }

    public static class ExpandStation implements IndustryOptionTrampoline {
        @Override
        public void initialiseOptionsFromJSON(JSONObject data) throws JSONException {
            Boggled_Expand_Station.settingsFromJSON(data);
        }
    }

    public static class Genelab implements IndustryOptionTrampoline {
        @Override
        public void initialiseOptionsFromJSON(JSONObject data) throws JSONException {
            Boggled_Genelab.settingsFromJSON(data);
        }
    }

    public static class GPA implements IndustryOptionTrampoline {
        @Override
        public void initialiseOptionsFromJSON(JSONObject data) throws JSONException {
            Boggled_GPA.settingsFromJSON(data);
        }
    }

    public static class HarmonicDamper implements IndustryOptionTrampoline {
        @Override
        public void initialiseOptionsFromJSON(JSONObject data) throws JSONException {
            Boggled_Harmonic_Damper.settingsFromJSON(data);
        }
    }

    public static class Hydroponics implements IndustryOptionTrampoline {
        @Override
        public void initialiseOptionsFromJSON(JSONObject data) throws JSONException {
            Boggled_Hydroponics.settingsFromJSON(data);
        }
    }

    public static class IsmaraSling implements IndustryOptionTrampoline {
        @Override
        public void initialiseOptionsFromJSON(JSONObject data) throws JSONException {
            Boggled_Ismara_Sling.settingsFromJSON(data);
        }
    }

    public static class KletkaSimulator implements IndustryOptionTrampoline {
        @Override
        public void initialiseOptionsFromJSON(JSONObject data) throws JSONException {
            Boggled_Kletka_Simulator.settingsFromJSON(data);
        }
    }

    public static class LimelightNetwork implements IndustryOptionTrampoline {
        @Override
        public void initialiseOptionsFromJSON(JSONObject data) throws JSONException {
            Boggled_Limelight_Network.settingsFromJSON(data);
        }
    }

    public static class Magnetoshield implements IndustryOptionTrampoline {
        @Override
        public void initialiseOptionsFromJSON(JSONObject data) throws JSONException {
            Boggled_Magnetoshield.settingsFromJSON(data);
        }
    }

    public static class MesozoicPark implements IndustryOptionTrampoline {
        @Override
        public void initialiseOptionsFromJSON(JSONObject data) throws JSONException {
            Boggled_Mesozoic_Park.settingsFromJSON(data);
        }
    }

    public static class OuyangOptimizer implements IndustryOptionTrampoline {
        @Override
        public void initialiseOptionsFromJSON(JSONObject data) throws JSONException {
            Boggled_Ouyang_Optimizer.settingsFromJSON(data);
        }
    }

    public static class PerihelionProject implements IndustryOptionTrampoline {
        @Override
        public void initialiseOptionsFromJSON(JSONObject data) throws JSONException {
            Boggled_Perihelion_Project.settingsFromJSON(data);
        }
    }

    public static class PlanetCracker implements IndustryOptionTrampoline {
        @Override
        public void initialiseOptionsFromJSON(JSONObject data) throws JSONException {
            Boggled_Planet_Cracker.settingsFromJSON(data);
        }
    }

    public static class PlanetaryAgravField implements IndustryOptionTrampoline {
        @Override
        public void initialiseOptionsFromJSON(JSONObject data) throws JSONException {
            Boggled_Planetary_Agrav_Field.settingsFromJSON(data);
        }
    }

    public static class RemnantStation implements IndustryOptionTrampoline {
        @Override
        public void initialiseOptionsFromJSON(JSONObject data) throws JSONException {
            Boggled_Remnant_Station.settingsFromJSON(data);
        }
    }

    public static class StellarReflectorArray implements IndustryOptionTrampoline {
        @Override
        public void initialiseOptionsFromJSON(JSONObject data) throws JSONException {
            Boggled_Stellar_Reflector_Array.settingsFromJSON(data);
        }
    }
}
