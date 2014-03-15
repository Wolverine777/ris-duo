package app;

import static app.nodes.NodeFactory.nodeFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import vecmath.Matrix;
import vecmath.Vector;
import vecmath.vecmathimp.MatrixImp;
import vecmath.vecmathimp.VectorImp;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import app.Types.ObjectTypes;
import app.Types.PhysicType;
import app.Types.SimulateType;
import app.edges.Edge;
import app.eventsystem.FloorCreation;
import app.eventsystem.NodeCreation;
import app.eventsystem.NodeDeletion;
import app.eventsystem.NodeModification;
import app.eventsystem.PhysicModification;
import app.messages.Message;
import app.messages.PhysicInitialization;
import app.messages.SingelSimulation;
import app.nodes.Node;
import app.nodes.shapes.Shape;
import app.toolkit.StopWatch;

public class Physic extends UntypedActor {

	private Map<String, Node> nodes = new HashMap<String, Node>();
	private Map<String, Node> nodesCollisionOnly = new HashMap<String, Node>();
	private Map<String, Vector> impacts = new HashMap<String, Vector>();
	ActorRef simulator;
	ActorRef ai;
	private StopWatch zeit = new StopWatch();
	private Vector ground = new VectorImp(0f, -0.005f, 0f);
	private float elapsedaverage = 0;
	private float elapsed = 0;
	private float elapsedCounter = 1;
	private Vector floor;

	private void initialize() {
		getSender().tell(Message.INITIALIZED, self());
		System.out.println("Physic initialised");
		elapsedaverage = zeit.elapsed();
	}

	public void physic() {
		System.out.println("physic loop");
		if(elapsedCounter == 1){
			elapsedaverage = 0;
			elapsedCounter++;
		}
	
		elapsedaverage = (elapsed + zeit.elapsed())/elapsedCounter;
		elapsed = zeit.elapsed();
		NodeDeletion delete = new NodeDeletion();
		for (Node n : nodes.values()) {
			if (collisionGround(n) == 0 && collisionObjects(n) == null) {
			
				n.setForce((n.getVelocity().add(new VectorImp(0, ground.y()* n.getMass()* elapsed, 0))));
				
				n.setVelocity(n.getForce());
				
					
				PhysicModification p = new PhysicModification();
				p.id = n.getId();
				p.force = n.getForce();
				
				simulator.tell(p, self());

			} else if (collisionGround(n) != 0
					&& collisionObjects(n) == null) {
				
				if(((Shape)n).getLifetimeCounter() > 0) {
					
					System.out.println("lifetimecounter: " + ((Shape)n).getLifetimeCounter());
					((Shape)n).setLifetimeCounter(((Shape)n).getLifetimeCounter()-1);
					VectorImp opposite = oppositeDirectionGround(n);
					n.setVelocity(opposite);
					VectorImp reduce = reduceVelocityGround(n, 0.9f);
					n.setVelocity(reduce);
					// TODO Erdanziehungskraft m*g?
	//				n.setForce((n.getVelocity().add(new VectorImp(0, ground.y()* elapsed, 0))));
					// TODO Masse einabauen, dann impuls setzen und dann velocity
					n.setForce(n.getVelocity());
					n.setVelocity(n.getForce());
	//				
					/*TODO: nicht mehr verschieben, ansonsten SingelSimulation
					 *  Oder Map mit Nodes,Matrix und nur intern verschieben, damit collision nix mehr erkennt und den richtingen verschiebungsvektor setzt, 
					 *  danach bei nModifikation recive aus der map die matrix holen, damit interne rapr�. mit anderen actors gleich bleibt
					 *  vorteil, interne verschiebung nicht sichtbar. also sieht man nur die kugel in das andere object rein gehen, aber danach auch wieder raus
					 */
					
					float differenceinfloor = collisionGround(n);
	//				float differenceinfloor = (float) Math.sqrt((float) Math.pow((n.getWorldTransform().getPosition().y() - floor.y()),2));
					VectorImp vec = new VectorImp(0, differenceinfloor + 0.05f, 0); // 1 ist der Radius der Kugeln + 0.01 damit immer knapp �ber dem boden
					SingelSimulation ss = new SingelSimulation(n.getId(), SimulateType.DRIVE, vec, n.getWorldTransform());
	//				Matrix modify=MatrixImp.translate(vec);
	//	    		n.updateWorldTransform(modify);
	//	    		getSender().tell(new NodeModification(n.id,modify), self());
					
					simulator.tell(ss, self());
				
					PhysicModification p1 = new PhysicModification();
					p1.id = n.getId();
					p1.force = n.getForce();
					
					
					simulator.tell(p1, self());	
				}
				else{
					System.out.println("lifetimecounter unten: " + ((Shape)n).getLifetimeCounter());
					delete.ids.add(n.getId());
				}
				

			} else if (collisionGround(n) == 0
					&& collisionObjects(n) != null) {

				 
//				Node collision = collisionObjects(n);

							
				delete.ids.add(n.getId());
				

			} else if(collisionGround(n) !=0 && collisionObjects(n) !=null){
				
				delete.ids.add(n.getId());
			}
//			Vector impact = collisionGroundPosition(n);
//			System.out.println("IMpact oben: " + impact.toString());
		}
		
		for (Node n : nodesCollisionOnly.values()) {
			if(collisionObjects(n) != null ){
				delete.ids.add(n.getId());
				
			}
			
		}
		
		if(delete.ids.isEmpty() != true){
			for(String id: delete.ids){
				nodes.remove(id);
			}
			for(String id: delete.ids){
				nodesCollisionOnly.remove(id);
			}
			getSender().tell(delete, self());
			
		}
		
		getSender().tell(Message.DONE, self());
//		System.out.println("Impacts: " + impacts.toString());
	}

	// TODO: Im Moment gibt er die Node aus der liste nodes zuerst aus + Problem wenn Collision mit mehreren Objekten vorhanden ist
	// 		 M�gliche L�sung w�re eine Liste von nodes anzulegen und diese zur�ckzugeben.
	private Node collisionObjects(Node n) {
		float distance = 0;
		float radiuses = 0;
		for (Node node : nodes.values()) {
			if (!node.equals(n)) {
//				System.out.println("Center n: " + ((Shape) n).getCenter());
				distance = ((Shape) n).getCenter().sub(((Shape) node).getCenter()).length();
				radiuses = (((Shape) n).getRadius() + ((Shape) node).getRadius());
//				System.out.println("Radius n: " + ((Shape) n).getRadius()
//						+ "Radius node: " + ((Shape) node).getRadius()
//						+ "distance1: " + distance + "radiuses1: " + radiuses);
				if (distance < radiuses) {
//					System.out.println("distance2: " + distance + "radiuses2: "
//							+ radiuses);
					return node;
				}
			}
		}
		for (Node node : nodesCollisionOnly.values()) {
			if (!node.equals(n)) {
//				System.out.println("Center n: " + ((Shape) n).getCenter());
				distance = ((Shape) n).getCenter().sub(((Shape) node).getCenter()).length();
				radiuses = (((Shape) n).getRadius() + ((Shape) node).getRadius());
//				System.out.println("Radius n: " + ((Shape) n).getRadius()
//						+ "Radius node: " + ((Shape) node).getRadius()
//						+ "distance1: " + distance + "radiuses1: " + radiuses);
				if (distance < radiuses) {
//					System.out.println("distance2: " + distance + "radiuses2: "
//							+ radiuses);
					return node;
				}
			}
		}
		return null;
	}
	
	private void collisionGroundPosition(String id, Node n){
		
		float durchlauf = 0;
//		System.out.println("collision ground posi: " + n.getId());
		
		
				
		while(collisionGround(n)==0){
			
//			System.out.println("Durchlauf Nr: " + durchlauf);
			
			n.setForce((n.getVelocity().add(new VectorImp(0, ground.y()* n.getMass()* elapsedaverage, 0))));
			
			n.setVelocity(n.getForce());
			
			Matrix modify=MatrixImp.translate(n.getForce());
    		n.updateWorldTransform(modify);
			
    		durchlauf++;
		}
		VectorImp impact = new VectorImp(n.getWorldTransform().getPosition().x(), floor.y(), n.getWorldTransform().getPosition().z());
		
		System.out.println("Aufprallort: " + n.getId() + impact + "Elapsed: " + elapsedaverage);
		impacts.put(id, impact);
		
		PhysicModification tellAi = new PhysicModification();
		tellAi.force = impact;
		tellAi.id = id;
		ai.tell(tellAi, self());
		
		
	}
	

	private float collisionGround(Node n) {
		float distance = 0;
		float radiuses = 0;
		
		
//		System.out.println("flooooooor: " + floor.y());
		distance = (float) Math.sqrt((float) Math.pow(((Shape) n).getCenter().y() - floor.y(),2));
		radiuses = ((Shape) n).getRadius();
//		System.out.println("distance ground: " + distance + " radiuses ground: " + radiuses);
			
		if(distance < radiuses){
			float inground = radiuses -distance;
					return inground;
				
		
		}
		return 0;
	}
	
	private VectorImp oppositeDirection(Node n){
		float x = n.getVelocity().x();
		float y = n.getVelocity().y();
		float z = n.getVelocity().z();
		
		x = -1* x;
		
		VectorImp newVelo = new VectorImp(x, y, z);
		
		return newVelo;
		
		
	}
	
	private VectorImp oppositeDirectionGround(Node n){
		float x = n.getVelocity().x();
		float y = n.getVelocity().y();
		float z = n.getVelocity().z();
		
		y = -1* y;
		
		VectorImp newVelo = new VectorImp(x, y, z);
		
		return newVelo;
		
	}
	
	private VectorImp reduceVelocityGround(Node n, Float reduceBy){
		
		float x = n.getVelocity().x();
		float y = n.getVelocity().y();
		float z = n.getVelocity().z();
		
		y = reduceBy *y;
		
		VectorImp newVelo = new VectorImp(x, y, z);
		
		return newVelo;
	}

	public void onReceive(Object message) throws Exception {
		if (message == Message.LOOP) {
			physic();
		} else if (message instanceof PhysicInitialization) {
			this.simulator = (((PhysicInitialization) message).simulator);
			this.ai = (((PhysicInitialization) message).ai);
			initialize();
		} else if (message instanceof NodeCreation) {
			
			if (((NodeCreation) message).type == ObjectTypes.GROUP) {
				Node newNode = nodeFactory
						.groupNode(((NodeCreation) message).id);
				if(((NodeCreation) message).physicType == PhysicType.Physic_complete){
					
					nodes.put(newNode.getId(), newNode);
				}
				if(((NodeCreation) message).physicType == PhysicType.Collision_only){
					nodesCollisionOnly.put(newNode.getId(), newNode);
				}
			} else if (((NodeCreation) message).type == ObjectTypes.CUBE) {

				Node newNode = nodeFactory.cube(((NodeCreation) message).id,
						((NodeCreation) message).shader,
						((NodeCreation) message).w, ((NodeCreation) message).h,
						((NodeCreation) message).d, ((NodeCreation) message).mass);
				if ((((NodeCreation) message).impulse != null)) {
					Vector impulse = (((NodeCreation) message).impulse);
					float newx = impulse.x()/newNode.mass;
					float newy = impulse.y()/newNode.mass;
					float newz = impulse.z()/newNode.mass;
					
					VectorImp newimpulse = new VectorImp(newx, newy, newz);
					newNode.setVelocity(newimpulse);
				}
				if ((((NodeCreation) message).modelmatrix != null)) {
					newNode.updateWorldTransform(((NodeCreation) message).modelmatrix);
				}
				if ((((NodeCreation) message).center != null)) {
					((Shape) newNode)
							.setCenter(((NodeCreation) message).center);
				}
				if ((((NodeCreation) message).radius != 0)) {
					((Shape) newNode)
							.setRadius(((NodeCreation) message).radius);
				}
				if(((NodeCreation) message).physicType == PhysicType.Physic_complete){
					
					nodes.put(newNode.getId(), newNode);
				}
				if(((NodeCreation) message).physicType == PhysicType.Collision_only){
					nodesCollisionOnly.put(newNode.getId(), newNode);
				}
			} else if (((NodeCreation) message).type == ObjectTypes.SPHERE) {

				Node newNode = nodeFactory.sphere(((NodeCreation) message).id,
						((NodeCreation) message).shader, ((NodeCreation) message).mass);
				
				Node newNode2 = nodeFactory.sphere("hallo",
						((NodeCreation) message).shader, ((NodeCreation) message).mass);

				if ((((NodeCreation) message).impulse != null)) {
					Vector impulse = (((NodeCreation) message).impulse);
			
					float newx = impulse.x()/newNode.mass;
					float newy = impulse.y()/newNode.mass;
					float newz = impulse.z()/newNode.mass;
					
					VectorImp newimpulse = new VectorImp(newx, newy, newz);
					newNode.setVelocity(newimpulse);
					newNode2.setVelocity(newimpulse);

				}
				if ((((NodeCreation) message).modelmatrix != null)) {
					newNode.updateWorldTransform(((NodeCreation) message).modelmatrix);
					newNode2.updateWorldTransform(((NodeCreation) message).modelmatrix);
				}
				if ((((NodeCreation) message).center != null)) {
					((Shape) newNode)
							.setCenter(((NodeCreation) message).center);
					((Shape) newNode2)
							.setCenter(((NodeCreation) message).center);
				}
				if ((((NodeCreation) message).radius != 0)) {
					((Shape) newNode)
							.setRadius(((NodeCreation) message).radius);
					((Shape) newNode2)
							.setRadius(((NodeCreation) message).radius);
				}
				if(((NodeCreation) message).physicType == PhysicType.Physic_complete){
					
					nodes.put(newNode.getId(), newNode);
				}
				if(((NodeCreation) message).physicType == PhysicType.Collision_only){
					nodesCollisionOnly.put(newNode.getId(), newNode);
				}
				collisionGroundPosition(newNode.getId(), newNode2);
			} else if(((NodeCreation) message).type == ObjectTypes.OBJECT){
				NodeCreation nc=(NodeCreation) message;
				Node newNode = nodeFactory.obj(nc.id, nc.shader, nc.sourceFile, nc.sourceTex, nc.mass);
				
				if ((((NodeCreation) message).impulse != null)) {
					Vector impulse = (((NodeCreation) message).impulse);
					float newx = impulse.x()/newNode.mass;
					float newy = impulse.y()/newNode.mass;
					float newz = impulse.z()/newNode.mass;
					
					VectorImp newimpulse = new VectorImp(newx, newy, newz);
					newNode.setVelocity(newimpulse);
				}
				if ((((NodeCreation) message).modelmatrix != null)) {
					newNode.updateWorldTransform(((NodeCreation) message).modelmatrix);
				}
				if ((((NodeCreation) message).center != null)) {
					((Shape) newNode)
							.setCenter(((NodeCreation) message).center);
				}
				if ((((NodeCreation) message).radius != 0)) {
					((Shape) newNode)
							.setRadius(((NodeCreation) message).radius);
					System.out.println("center objtest physic: " + ((Shape) newNode).getCenter() + "Position: " + newNode.getWorldTransform().getPosition());
				}
				if(((NodeCreation) message).physicType == PhysicType.Physic_complete){
					
					nodes.put(newNode.getId(), newNode);
				}
				if(((NodeCreation) message).physicType == PhysicType.Collision_only){
					nodesCollisionOnly.put(newNode.getId(), newNode);
				}
			}
		} else if (message instanceof NodeModification) {
			// System.out.println("NODEMODIFICATION!!!!!");
			if (nodes.containsKey(((NodeModification) message).id)) {
				// System.out.println("NodeModification");

				// System.out.println("Nodes " + nodes);
				// System.out.println("Accesing "
				// + ((NodeModification) message).id);

				Node modify = nodes.get(((NodeModification) message).id);

				if (((NodeModification) message).localMod != null) {
					// modify.setLocalTransform(((NodeModification)
					// message).localMod);
					// modify.updateWorldTransform();
					modify.updateWorldTransform(((NodeModification) message).localMod);
				}

			}
			if (nodesCollisionOnly.containsKey(((NodeModification) message).id)) {
				

				Node modify = nodesCollisionOnly.get(((NodeModification) message).id);

				if (((NodeModification) message).localMod != null) {
					
					modify.updateWorldTransform(((NodeModification) message).localMod);
				}

			}
		}
		else if( message instanceof FloorCreation){
			floor = ((FloorCreation) message).position;			
			
		}  else if (message instanceof NodeDeletion){
			NodeDeletion delete = (NodeDeletion)message;
			for(String id: delete.ids){
				Node modify = nodes.get(id);
				ArrayList<Edge> removeEdges = new ArrayList<>(); 
				if(modify!=null){
				for(Edge e: modify.getEdges()){
					removeEdges.add(e);
//					nodes.get(e.getOtherNode(modify).id).removeEdge(e);
					
				}
				for(Edge e : removeEdges){
					modify.removeEdge(e);
				}
			
				nodes.remove(modify);
				}
			}
			for(String id: delete.ids){
				Node modify1 = nodesCollisionOnly.get(id);
				ArrayList<Edge> removeEdges = new ArrayList<>(); 
				if(modify1!=null){
				for(Edge e: modify1.getEdges()){
					removeEdges.add(e);
//					nodes.get(e.getOtherNode(modify).id).removeEdge(e);
					
				}
				for(Edge e : removeEdges){
					modify1.removeEdge(e);
				}
			
				nodesCollisionOnly.remove(modify1);
				}
			}
		}
		
	}
}
