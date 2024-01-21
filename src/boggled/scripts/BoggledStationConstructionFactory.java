package boggled.scripts;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BoggledStationConstructionFactory {
    public interface StationConstructionFactory {
        BoggledStationConstructors.StationConstructionData constructFromJSON(String id, String data) throws JSONException;
    }

    public static class AstropolisConstructionFactory implements StationConstructionFactory {
        @Override
        public BoggledStationConstructors.StationConstructionData constructFromJSON(String id, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);

            JSONArray industriesToQueueArray = jsonData.optJSONArray("industries_to_queue");
            List<String> industriesToQueue = new ArrayList<>();
            if (industriesToQueueArray != null) {
                for (int i = 0; i < industriesToQueueArray.length(); ++i) {
                    industriesToQueue.add(industriesToQueueArray.getString(i));
                }
            }

            return new BoggledStationConstructors.AstropolisConstructionData("boggled_astropolis", industriesToQueue);
        }
    }

    public static class MiningStationConstructionFactory implements StationConstructionFactory {
        @Override
        public BoggledStationConstructors.StationConstructionData constructFromJSON(String id, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);

            JSONArray industriesToQueueArray = jsonData.optJSONArray("industries_to_queue");
            List<String> industriesToQueue = new ArrayList<>();
            if (industriesToQueueArray != null) {
                for (int i = 0; i < industriesToQueueArray.length(); ++i) {
                    industriesToQueue.add(industriesToQueueArray.getString(i));
                }
            }

            JSONArray resourcesToHighlightArray = jsonData.optJSONArray("resources_to_highlight");
            List<String> resourcesToHighlight = new ArrayList<>();
            for (int i = 0; i < resourcesToHighlightArray.length(); ++i) {
                resourcesToHighlight.add(resourcesToHighlightArray.getString(i));
            }

            return new BoggledStationConstructors.MiningStationConstructionData("boggled_mining", industriesToQueue, resourcesToHighlight);
        }
    }

    public static class SiphonStationConstructionFactory implements StationConstructionFactory {
        @Override
        public BoggledStationConstructors.StationConstructionData constructFromJSON(String id, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);

            JSONArray industriesToQueueArray = jsonData.optJSONArray("industries_to_queue");
            List<String> industriesToQueue = new ArrayList<>();
            if (industriesToQueueArray != null) {
                for (int i = 0; i < industriesToQueueArray.length(); ++i) {
                    industriesToQueue.add(industriesToQueueArray.getString(i));
                }
            }

            JSONArray resourcesToHighlightArray = jsonData.optJSONArray("resources_to_highlight");
            List<String> resourcesToHighlight = new ArrayList<>();
            for (int i = 0; i < resourcesToHighlightArray.length(); ++i) {
                resourcesToHighlight.add(resourcesToHighlightArray.getString(i));
            }

            return new BoggledStationConstructors.SiphonStationConstructionData("boggled_siphon", industriesToQueue, resourcesToHighlight);
        }
    }
}
