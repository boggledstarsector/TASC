package data.scripts;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;

public class BoggledProjectRequirementsAND implements Iterable<BoggledProjectRequirementsAND.RequirementWithTooltipOverride> {
    public static class RequirementWithTooltipOverride {
        private BoggledProjectRequirementsOR requirements;
        private String tooltipOverride;

        public RequirementWithTooltipOverride(BoggledProjectRequirementsOR requirements, String tooltipOverride) {
            this.requirements = requirements;
            this.tooltipOverride = tooltipOverride;
        }

        public boolean checkRequirement(MarketAPI market) {
            return requirements.checkRequirement(market);
        }
        public String getTooltip() {
            if (tooltipOverride.isEmpty()) {
                return requirements.getTooltip();
            }
            return tooltipOverride;
        }
    }

    private final ArrayList<RequirementWithTooltipOverride> requirementsOR;

    @NotNull
    @Override
    public Iterator<RequirementWithTooltipOverride> iterator() {
        return requirementsOR.iterator();
    }

    public boolean add(RequirementWithTooltipOverride req) {
        return requirementsOR.add(req);
    }

    public boolean requirementsMet(MarketAPI market) {
        for (BoggledProjectRequirementsAND.RequirementWithTooltipOverride req : requirementsOR) {
            if (!req.checkRequirement(market)) {
                return false;
            }
        }
        return true;
    }

    public BoggledProjectRequirementsAND(ArrayList<RequirementWithTooltipOverride> requirementsOR) {
        this.requirementsOR = requirementsOR;
    }
}
