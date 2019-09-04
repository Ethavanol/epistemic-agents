package eis.percepts;


import eis.percepts.terrain.Terrain;
import eis.percepts.things.Thing;
import utils.Graph;
import utils.Position;

import java.util.ConcurrentModificationException;

/**
 * This class should contain the type of the terrain, the absolute position, and the last perceived step
 */
public class MapPercept {
    private Position location;
    private String agentSource;
    private Terrain terrain;
    private Thing thing;
    private long lastStepPerceived;

    public MapPercept(MapPercept percept)
    {
        this(percept.getLocation().clone(), percept.agentSource, percept.lastStepPerceived);
        this.setTerrain(percept.terrain);
        this.setThing(percept.thing);
    }

    public MapPercept(Position location, String agentSource, long lastStepPerceived)
    {
        this.location = location;
        this.agentSource = agentSource;
        this.lastStepPerceived = lastStepPerceived;
    }

    public Position getLocation() {
        return location;
    }

    public Terrain getTerrain() {
        return terrain;
    }

    public Thing getThing() {
        return thing;
    }

    public void setTerrain(Terrain terrain) {
        if(terrain == null)
        {
            this.terrain = null;
            return;
        }

        this.terrain = terrain.clone();
        setLocation(this.location);
    }

    public void setThing(Thing thing) {
        if(thing == null)
        {
            this.thing = null;
            return;
        }

        this.thing = thing.clone();
        setLocation(this.location);
    }

    public boolean isBlocking()
    {
        return (terrain != null && terrain.isBlocking()) || (thing != null && thing.isBlocking());
    }

    public long getLastStepPerceived() {
        return lastStepPerceived;
    }

    public MapPercept copyToAgent(Position translation) {
        MapPercept newPercept = new MapPercept(this);
        newPercept.setLocation(this.getLocation().subtract(translation));

        return newPercept;
    }

    public void setLocation(Position newPos) {
        if(thing != null)
            thing.setPosition(newPos);

        if(terrain != null)
            terrain.setPosition(newPos);

        this.location = newPos;
    }

    public String getAgentSource() {
        return agentSource;
    }
}
