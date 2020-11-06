package epistemic;

import epistemic.distribution.propositions.MultiValueProposition;
import epistemic.distribution.propositions.Proposition;
import epistemic.distribution.propositions.SingleValueProposition;
import epistemic.wrappers.NormalizedWrappedLiteral;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import epistemic.wrappers.WrappedLiteral;
import jason.asSyntax.Term;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * The World class is a map of initial literals to possible enumerations.
 * <p>
 * Random note: What-if indistinguishable worlds mapped to the same hashCode? This would need to be dynamic.
 * <p>
 * The key should be Hashed based on the number of variables (?), as it needs to hash based on the following examples:
 * 1. { alice(_), bob(_) } -> { alice(_), bob(_) }
 * 2. { hand("Alice", _), hand("Bob", _)  } -> { hand("Alice", _), hand("Bob", _)  }
 * 3. { alice("AA"), alice("BB") } -> { alice(_) }
 * <p>
 * <p>
 * { alice(Hand), alice("AA") } -> { alice\1 }
 * { hand(Player, Hand), hand("Alice", Alice), hand("Bob", Bob) } -> hand\2 isn't sufficient ("Alice" and "Bob" should be considered separate).
 */
public class World {

    private final Map<WrappedLiteral, Proposition> propositionMap;
    private final Set<WrappedLiteral> cachedWrappedValues;

    public World() {
        this.propositionMap = new HashMap<>();
        this.cachedWrappedValues = new HashSet<>();
    }

    protected World(World world) {
        this();
        this.propositionMap.putAll(world.propositionMap);
        this.cachedWrappedValues.addAll(world.cachedWrappedValues);
    }

    @Deprecated
    public void putLiteral(@NotNull WrappedLiteral key, @NotNull Literal value) {
        if(!containsKey(key))
        {
            this.putProposition(new SingleValueProposition(key, value));
            return;
        }

        Proposition currentProp = this.get(key);
        NormalizedWrappedLiteral wrappedValue = new NormalizedWrappedLiteral(value);

        // Already added to the world
        if(currentProp.getValue().contains(wrappedValue))
            return;

        if(currentProp instanceof SingleValueProposition)
            // Set to multi value
            currentProp = new MultiValueProposition(key.getNormalizedWrappedLiteral(),currentProp.getValue());

        currentProp.getValue().add(wrappedValue);
        this.putProposition(currentProp);

    }

    public void putProposition(Proposition proposition) {

        Proposition previous = propositionMap.put(proposition.getKey(), proposition);

        if (previous != null)
            this.cachedWrappedValues.removeAll(previous.getValue());

        // place value in value cache
        this.cachedWrappedValues.addAll(proposition.getValue());
    }

    public World clone() {
        return new World(this);
    }


    public Literal toLiteral() {
        Literal literal = ASSyntax.createLiteral("world");
        for (var term : propositionMap.values()) {
            for (var lit : term.getValue())
                literal.addTerm(lit.getCleanedLiteral());
        }

        return literal;
    }

    /**
     * Gets the set of WrappedLiteral values contained within the Proposition values.
     *
     * @return The set of all possible values.
     */
    public Set<WrappedLiteral> wrappedValueSet() {
        return cachedWrappedValues;
    }

    /**
     * @return A Proposition value set.
     */
    public Set<Proposition> valueSet() {
        return new HashSet<>(propositionMap.values());
    }

    public Set<WrappedLiteral> keySet() {
        return this.propositionMap.keySet();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        boolean firstVal = true;

        stringBuilder.append("{");

        for (var prop : propositionMap.values()) {
            for (var litValue : prop.getValue()) {
                if (!firstVal)
                    stringBuilder.append(", ");
                else
                    firstVal = false;

                // Don't show annotations when printing.
                stringBuilder.append(litValue.getCleanedLiteral().clearAnnots());
            }
        }

        stringBuilder
                .append("}");

        return stringBuilder.toString();
    }

    /**
     * Evaluate the belief in the current world
     *
     * @param belief The belief to evaluate.
     * @return True if the belief exists (and is true) in the world, and False otherwise.
     */
    public boolean evaluate(Literal belief) {
        if (belief == null)
            return false;

        WrappedLiteral key = new WrappedLiteral(belief);
        return wrappedValueSet().contains(key);
    }


    public String getUniqueName() {
        return String.valueOf(this.hashCode());
    }

    public boolean containsKey(WrappedLiteral curIndicator) {
        return propositionMap.containsKey(curIndicator);
    }

    public int size() {
        return propositionMap.size();
    }

    public Proposition get(WrappedLiteral key) {
        return propositionMap.get(key);
    }

    public void putAll(Map<WrappedLiteral, SingleValueProposition> entry) {
        this.propositionMap.putAll(entry);
    }

    @Override
    public int hashCode() {
        int hash = 1;

        // The hash code of the world should be values only (entries causes collisions due to key being repeated in value prop)
        for (var prop : wrappedValueSet())
            hash = 31 * hash + prop.toSafePropName().hashCode();

        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof World))
            return false;

        var otherWorld = (World) obj;
        return propositionMap.equals(otherWorld.propositionMap);
    }

}
