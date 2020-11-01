package epistemic.reasoner.stub;

import epistemic.ManagedWorlds;
import epistemic.formula.EpistemicFormula;
import epistemic.reasoner.ReasonerSDK;
import epistemic.wrappers.WrappedLiteral;

import java.util.*;
import java.util.stream.Collectors;

public class StubReasonerSDK extends ReasonerSDK {
    private ManagedWorlds curManagedWorlds;
    private final Map<EpistemicFormula, Boolean> formulaValuations;
    private Map<WrappedLiteral, Set<WrappedLiteral>> currentPropositionValues;
    private boolean defaultValuation;

    public StubReasonerSDK() {
        super(null);
        defaultValuation = false;
        this.formulaValuations = new HashMap<>();
        currentPropositionValues = new HashMap<>();
    }

    @Override
    public void createModel(ManagedWorlds managedWorlds) {
        this.curManagedWorlds = managedWorlds;
        this.formulaValuations.clear();
        this.currentPropositionValues.clear();
    }

    public void setFormulaValuation(EpistemicFormula formula, boolean value) {
        formulaValuations.put(formula, value);
    }

    /**
     * Sets all formulas to true.
     */
    public void setFormulaValuation(Collection<EpistemicFormula> formulaList, boolean value) {
        for (var formula : formulaList)
            formulaValuations.put(formula, value);
    }

    public ManagedWorlds getCurrentModel() {
        return curManagedWorlds;
    }

    public Map<WrappedLiteral, Set<WrappedLiteral>> getCurrentPropositionValues() {
        return currentPropositionValues;
    }

    @Override
    public Map<EpistemicFormula, Boolean> evaluateFormulas(Collection<EpistemicFormula> formulas) {
        // Map all formulas to evaluate to true
        return formulas.stream()
                .map(formula -> new AbstractMap.SimpleEntry<>(formula, formulaValuations.getOrDefault(formula, defaultValuation)))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }

    @Override
    public Map<EpistemicFormula, Boolean> updateProps(Map<WrappedLiteral, Set<WrappedLiteral>> propositionValues, Collection<EpistemicFormula> epistemicFormulas) {
        this.currentPropositionValues = new HashMap<>(propositionValues);
        return this.evaluateFormulas(epistemicFormulas);
    }

    public void setDefaultValuation(boolean defaultValuation) {
        this.defaultValuation = defaultValuation;
    }
}
