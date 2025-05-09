package epistemic.agent;

import epistemic.distribution.EpistemicDistribution;
import epistemic.distribution.formula.EpistemicFormula;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.PredicateIndicator;
import jason.bb.BeliefBase;
import jason.bb.ChainBBAdapter;
import jason.bb.DefaultBeliefBase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class ChainedEpistemicBB extends ChainBBAdapter {
    private final EpistemicDistribution epistemicDistribution;
    private final EpistemicAgent epistemicAgent;

    public ChainedEpistemicBB(BeliefBase beliefBase, EpistemicAgent agent, EpistemicDistribution distribution) {
        super(beliefBase != null ? beliefBase : new DefaultBeliefBase());
        this.epistemicDistribution = distribution;
        this.epistemicAgent = agent;
    }

    @Override
    public boolean add(Literal l) {
        var addRes = super.add(l);

        if(addRes)
            addToValuation(l);

        return addRes;
    }

    @Override
    public boolean add(int index, Literal l) {
        var addRes = super.add(index, l);

        if(addRes)
            addToValuation(l);

        return addRes;
    }

    private void addToValuation(Literal l) {

    }

    @Override
    public Literal contains(Literal l) {
        return super.contains(l);
    }

    @Override
    public Iterator<Literal> getCandidateBeliefs(PredicateIndicator pi) {
        // The predicate indicator doesn't work with epistemic beliefs,
        // i.e: it will always be knows/1, we need the root literal to be able to evaluate anything
        // We just forward this to the actual belief base.
        return super.getCandidateBeliefs(pi);
    }

    @Override
    public Iterator<Literal> getCandidateBeliefs(Literal l, Unifier u) {
        // Copy & Apply the unifier to the literal
        Literal unifiedLiteral = (Literal) l.capply(u);

        var epistemicLiteral = EpistemicFormula.fromLiteral(unifiedLiteral);
        // Unified literal is not an epistemic literal.
        if (epistemicLiteral == null)
            return super.getCandidateBeliefs(l, u);

        // If the literal is not managed by us, we delegate to the chained BB.
        if (!epistemicDistribution.getManagedWorlds().getManagedLiterals().isManagedBelief(epistemicLiteral.getRootLiteral().getNormalizedIndicator())) {
            epistemicAgent.getLogger().fine("The root literal in the epistemic formula: " + epistemicLiteral.getCleanedOriginal() + " is not managed by the reasoner. Delegating to BB.");
            return super.getCandidateBeliefs(l, u);
        }

        // If model not created yet, no need to evaluate formulas
        // And we need this to get the rules from the BB
        if(!epistemicAgent.getEpistemic().getModelCreated()) {
            if(l.isVar()){
                return iterator();
            } else {
                DefaultBeliefBase bb = (DefaultBeliefBase) this.nextBB;
                Map<PredicateIndicator, DefaultBeliefBase.BelEntry> belsMap = bb.getBelsMapDefaultNS();
                if (l.getNS() != Literal.DefaultNS) {
                    Atom ns = l.getNS();
                    if (ns.isVar()) {
                        l = (Literal)l.capply(u);
                        ns = l.getNS();
                    }
                    if (ns.isVar()) { // still a var
                        return iterator();
                    }
                    belsMap = bb.getNameSpacesFull().get(ns);
                }
                if (belsMap == null)
                    return null;
                DefaultBeliefBase.BelEntry entry = belsMap.get(l.getPredicateIndicator());
                if (entry != null) {
                    return bb.new EntryIteratorWrapper(entry);
                } else {
                    return null;
                }
            }
        }

        // If the root literal is not ground, then obtain all possible managed unifications
        var groundFormulas = epistemicAgent.getCandidateFormulas(epistemicLiteral);

        var result = epistemicDistribution.evaluateFormulas(groundFormulas);
        var arr = new ArrayList<Literal>();

        // If the result is true (formula evaluated to true), then return the literal as a candidate belief
        for(var formulaResultEntry : result.entrySet()) {
            // Add formula literal to results list if the formula was evaluated to true
            if(formulaResultEntry.getValue())
                arr.add(formulaResultEntry.getKey().getCleanedOriginal());
        }

        // Return null if no candidates
        // This maintains the original BB functionality
        if(arr.isEmpty())
            return null;

        return arr.iterator();
    }

    @Override
    public boolean abolish(PredicateIndicator pi) {
        return super.abolish(pi);
    }

    @Override
    public boolean abolish(Atom namespace, PredicateIndicator pi) {
        return super.abolish(namespace, pi);
    }

    @Override
    public boolean remove(Literal l) {
        return super.remove(l);
    }


}
