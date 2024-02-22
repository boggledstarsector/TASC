package boggled.scripts;

import boggled.campaign.econ.boggledTools;
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

            return new BoggledStationConstructors.AstropolisConstructionData(boggledTools.BoggledTags.astropolisStation, industriesToQueue);
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

            return new BoggledStationConstructors.MiningStationConstructionData(boggledTools.BoggledTags.miningStation, industriesToQueue, resourcesToHighlight);
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

            return new BoggledStationConstructors.SiphonStationConstructionData(boggledTools.BoggledTags.siphonStation, industriesToQueue, resourcesToHighlight);
        }
    }
}
