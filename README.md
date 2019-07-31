# MASSim 2019

Contest Details and Simulator can be found at https://multiagentcontest.org/2019/.
The following Gradle dependencies must be obtained and installed manually:
- eismassim 4.0 (with dependencies). Found in the simulator release: https://github.com/agentcontest/massim_2019/releases

# Development Iterations
The following sections describe the various milestones for the Agent.

## Iteration 0: MVP
This milestone represents the minimal deliverables required to make a functional agent. The Agent should be able to:

1. Choose a task and parse requirements
2. Choose a requirement
3. Find block dispenser for requirement and align itself to the dispenser
4. Find Goal location and navigate to it
5. Submit Task

In this milestone, we make the following simplifications using the server config file provided by the simulation server:

1. No obstacles (no need to perform complicated pathfinding)
2. No Action failures
3. Agent has unlimited visibility (no exploring/searching required)
4. Only one Agent (no competing teams, no agent communication required)
5. Agent ignores task deadlines
6. Task requirements only require 1 block attached

## Iteration 1: Fully-Featured Agent
This milestone looks to improve the agent developed in Iteration 0 by making it more robust in a dynamic / unpredictable environment. The Agent should be able to perform all main tasks in Iteration 0, but without some of the simplifications. This agent should be able to perform in the competition on a very basic level.

We incorporate the following features:
1. Obstacle handling (requires pathfinding and keeping mental notes of obstacles as we pass them)
2. Handle Action failures (probablistic failures as well as edge cases, such as trying to move into an obstacle)
3. Agent has a limited visibility of 5 blocks (we now need to explore the map for dispensers, obstacles, etc.)
4. More than one Agent (with teammates and competing teams)
5. Agent checks task deadlines to see if task is expired.
6. Task requirements require more than 1 block attached (complex block patterns)

The following simplifications persist in this iteration:
1. Teammates do not have to communicate.


## Iteration 2: Robust Agent
This iteration looks to address the following:
1. Fix major bugs that persist from previous iterations
2. Handle all edge cases
3. Implement all failure plans
4. Refactor to create a more modularized Agent. (Making it easier to add-in other aspects such as complex pathfinding and temporal reasoning)


## Iteration 3: Temporal Agent
The goal of this iteration is to perform more advanced temporal reasoning about task deadlines. For example, can we estimate how long it will take to complete a task. 

## Iteration 4: Advanced Exploration and Pathfinding
One crucial limiting factor of our agent is that it will only have a visibility of 5 blocks on a map that will definitely be larger than 5x5. Our agent must be able to explore areas, and make mental notes of the positions of various things. We must also use these mental notes to build an internal model of a map to perform pathfinding so that the agent can navigate to a dispenser, block, goal, etc. In this iteration, it would be ideal to have the agent teammates communicate with eachother to collaborate on a mental model of the map.
