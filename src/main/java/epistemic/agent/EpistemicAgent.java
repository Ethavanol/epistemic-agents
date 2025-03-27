package epistemic.agent;

import epistemic.distribution.EpistemicDistribution;
import epistemic.distribution.SyntaxDistributionBuilder;
import epistemic.distribution.formula.EpistemicFormula;
import epistemic.wrappers.WrappedLiteral;
import jason.JasonException;
import jason.RevisionFailedException;
import jason.asSemantics.Agent;
import jason.asSemantics.Intention;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.PlanLibrary;
import jason.bb.BeliefBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EpistemicAgent extends Agent {

    private EpistemicDistribution epistemicDistribution;
    private final SyntaxDistributionBuilder distributionBuilder;

    public EpistemicAgent() {
        this(new SyntaxDistributionBuilder());
    }

    public EpistemicAgent(@NotNull SyntaxDistributionBuilder distributionBuilder) {
        super.pl = new EpistemicPlanLibrary(new PlanLibrary());
        this.distributionBuilder = distributionBuilder;

    }

    @Override
    public void load(String asSrc) throws Exception {
        super.load(asSrc);
        this.agentLoaded();
    }

    @Override
    public boolean addBel(Literal bel) throws RevisionFailedException {
        return super.addBel(bel);
    }

    @Override
    public void load(InputStream in, String sourceId) throws Exception {
        super.load(in, sourceId);
        this.agentLoaded();
    }

    /**
     * Called when the agent has been loaded and parsed from the source file.
     * The possible worlds distribution can be initialized here. (Once initial beliefs/rules are parsed)
     */
    protected void agentLoaded() {
        getLogger().info("Epistemic framework initialized. Creating Epistemic Distribution:");

        // Create the distribution after loading the agent successfully
        this.epistemicDistribution = distributionBuilder.createDistribution(this);

        // This will wrap the BB with a chained epistemic BB now that the distribution is set.
        this.setBB(this.getBB());

        // Call the distribution agent loaded function
        epistemicDistribution.agentLoaded();
    }

    public void rebuildDistribution() {
        logger.info("Rebuilding epistemic distribution");
        long initTime = System.nanoTime();

        // Create a new distribution and only grab the managed worlds
        // (keep our current distribution object, as it contains other data such as current props)
        this.epistemicDistribution.setUpdatedWorlds(distributionBuilder.createDistribution(this).getManagedWorlds());

        long endTime = System.nanoTime();
        logger.info("Rebuild time (ms): " + ((endTime - initTime) / 1000000));
    }

    @Override
    public void setPL(PlanLibrary pl) {
        // Ensure we always have a proxy plan library in place
        if (!(pl instanceof EpistemicPlanLibrary))
            pl = new EpistemicPlanLibrary(pl);

        super.setPL(pl);
    }


    /**
     * @return The current EpistemicDistribution. This will return null before {@link EpistemicAgent#agentLoaded()} is called.
     */
    @Nullable
    public EpistemicDistribution getEpistemicDistribution() {
        return epistemicDistribution;
    }

    @Override
    public EpistemicPlanLibrary getPL() {
        return (EpistemicPlanLibrary) super.getPL();
    }

    /**
     * Update managed world props in this function. We can still operate on beliefs.
     * Use super.addBel(), etc. to trigger the BRF (and generate events) for inferred knowledge.
     */
    @Override
    public int buf(Collection<Literal> percepts) {

        Set<Literal> newPercepts = new HashSet<>();
        // We have to track deletions ourselves
        Set<Literal> deletions = new HashSet<>();

        // Add current (old) percepts
        super.bb.getPercepts().forEachRemaining(deletions::add);

        // Update the BB with new percepts before updating the managed worlds
        int result = super.buf(percepts);

        if (percepts != null) {
            // Use processed percepts rather than passed-in percepts
            super.bb.getPercepts().forEachRemaining(newPercepts::add);
            // Remove all deletions that were maintained
            deletions.removeAll(newPercepts);
        } else
            deletions.clear(); // Remove any old percepts deletions

        if (this.epistemicDistribution != null)
            this.epistemicDistribution.buf(newPercepts, deletions, this.getPL().getSubscribedFormulas());

        return result;
    }

    /**
     * Controls the addition / deletion of a belief. This function gets executed when +belief, -belief, or -+belief is executed by the ExecInt function.
     * <p>
     * We need to confirm that the model is correct. i.e. all proposition keys should have one (and only one) corresponding value in the BB.
     *
     * @see Agent#brf(Literal, Literal, Intention)
     */
    @Override
    public List<Literal>[] brf(Literal beliefToAdd, Literal beliefToDel, Intention i) throws RevisionFailedException {
        var revisionResult = new RevisionResult();

        if (this.epistemicDistribution != null)
            revisionResult.addResult(this.epistemicDistribution.brf(beliefToAdd, beliefToDel));
        else
            return super.brf(beliefToAdd, beliefToDel, i);

        // Forward any revisions to the super class to add to / delete from the BB
        var superRevision = new RevisionResult();

        // Add all revised propositions to the BB and keep track of any further revisions
        for (var additions : revisionResult.getAdditions()) {
            superRevision.addResult(super.brf(additions, null, i));
        }

        // Add all revised propositions to the BB and keep track of any further revisions
        for (var deletion : revisionResult.getDeletions()) {
            superRevision.addResult(super.brf(null, deletion, i));
        }

        return superRevision.buildResult();
    }

    public void setBB(BeliefBase beliefBase) {
        if (this.epistemicDistribution != null && !(beliefBase instanceof ChainedEpistemicBB))
            beliefBase = new ChainedEpistemicBB(beliefBase, this, this.epistemicDistribution);

        super.setBB(beliefBase);
    }


    /**
     * Unifies all possible values to an ungrounded epistemic formula
     * (i.e. an epistemic formula with an ungrounded root literal)
     *
     * @param epistemicFormula The epistemic formula with an ungrounded root literal.
     * @return The set of all grounded literals that unify with epistemicFormula.
     */
    public Set<EpistemicFormula> getCandidateFormulas(@NotNull EpistemicFormula epistemicFormula) {
        Set<EpistemicFormula> groundFormulaSet = new HashSet<>();


        // If the root literal is already ground, return a set containing the ground formula.
        if (epistemicFormula.getRootLiteral().getCleanedLiteral().isGround()) {
            groundFormulaSet.add(epistemicFormula);
            return groundFormulaSet;
        }

        // Obtain all possible proposition values from the managed literals object and attempt to unify each value with the ungrounded epistemic formula.
        // All formulas that can be successfully ground will be added to the set.

        // TODO: This has issues. finding by predicate indicator does not incorporate negation in the way that we'd like (i.e. ignore it)
        for (WrappedLiteral managedValue : epistemicDistribution.getManagedBeliefs(epistemicFormula.getRootLiteral().getNormalizedIndicator())) {
            // Create a cloned/normalized & ungrounded root literal to unify with
            var ungroundedLiteral = epistemicFormula.getRootLiteral().getNormalizedWrappedLiteral();
            var managedLiteral = managedValue.getNormalizedWrappedLiteral();

            // Attempt to unify with the various managed propositions
            Unifier unifier = ungroundedLiteral.unifyWrappedLiterals(managedLiteral);

            if (unifier != null) {
                var unifiedFormula = epistemicFormula.capply(unifier);

                if (!unifiedFormula.getCleanedOriginal().isGround()) {
                    getLogger().warning("formula " + epistemicFormula + " is still not ground after unifying: " + unifiedFormula);
                    continue;
                }

                groundFormulaSet.add(unifiedFormula);
            }
        }

        return groundFormulaSet;
    }

    protected SyntaxDistributionBuilder getDistributionBuilder() {
        return this.distributionBuilder;
    }
}
