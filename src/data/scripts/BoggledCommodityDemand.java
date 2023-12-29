package data.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.campaign.econ.boggledTools;

import java.awt.*;

public class BoggledCommodityDemand {
    public static abstract class CommodityDemand {
        protected final String[] enableSettings;
        protected String commodity;

        public CommodityDemand(String[] enableSettings, String commodity) {
            this.enableSettings = enableSettings;
            this.commodity = commodity;
        }

        protected abstract void applyDemandImpl(BaseIndustry industry);
        protected abstract void addPostDemandSectionImpl(String industryTooltip, BaseIndustry industry, TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode);

        public boolean isEnabled() {
            return boggledTools.optionsAllowThis(enableSettings);
        }

        public void applyDemand(BaseIndustry industry) {
            if (!isEnabled()) {
                return;
            }

            this.applyDemandImpl(industry);
        }

        public void addPostDemandSection(String industryTooltip, BaseIndustry industry, TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode) {
            if (!isEnabled()) {
                return;
            }

            this.addPostDemandSectionImpl(industryTooltip, industry, tooltip, hasDemand, mode);
        }
    }

    public static class Flat extends CommodityDemand {
        protected int quantity;
        public Flat(String[] enableSettings, String commodity, int quantity) {
            super(enableSettings, commodity);
            this.quantity = quantity;
        }
        @Override
        protected void applyDemandImpl(BaseIndustry industry) {
            industry.demand(commodity, quantity);
        }

        @Override
        public void addPostDemandSectionImpl(String industryTooltip, BaseIndustry industry, TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode) {
            float pad = 10.0f;
            Color highlight = Misc.getHighlightColor();
            String commodityTooltip = Global.getSettings().getCommoditySpec(commodity).getName();
            tooltip.addPara(industryTooltip + " always demand %s " + commodityTooltip + " regardless of market size.", pad, highlight, Integer.toString(quantity));
        }
    }

    public static class MarketSize extends CommodityDemand {
        protected int quantityOffset;
        public MarketSize(String[] enableSettings, String commodity, int quantityOffset) {
            super(enableSettings, commodity);
            this.quantityOffset = quantityOffset;
        }

        @Override
        protected void applyDemandImpl(BaseIndustry industry) {
            int adjustedQuantity = industry.getMarket().getSize() + quantityOffset;
            if (adjustedQuantity <= 0) {
                return;
            }
            industry.demand(commodity, adjustedQuantity);
        }

        @Override
        public void addPostDemandSectionImpl(String industryTooltip, BaseIndustry industry, TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode) {
        }
    }
}
