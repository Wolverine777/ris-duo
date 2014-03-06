package app;

import static app.nodes.NodeFactory.nodeFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import vecmath.Vector;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import app.Types.ObjectTypes;
import app.Types.SimulateType;
import app.datatype.AStarNodes;
import app.datatype.Level;
import app.datatype.LevelNode;
import app.datatype.Route;
import app.edges.Edge;
import app.eventsystem.NodeCreation;
import app.eventsystem.NodeDeletion;
import app.eventsystem.NodeModification;
import app.messages.AiInitialization;
import app.messages.Message;
import app.messages.SingelSimulation;
import app.nodes.Node;
import app.nodes.shapes.Car;
import app.nodes.shapes.Coin;

public class Ai extends UntypedActor {

	//for later implementation of changing routes because of ball is moving closer to floor.
	private final int UPDATEFREQUENZ=10;
	Level level;
	private Map<String, Node> nonAiNodes = new HashMap<String, Node>();
	private Map<String, Car> cars=new HashMap<String, Car>();
	private Map<String, Coin> coins=new HashMap<String, Coin>();
	ActorRef simulator;

	private void initialize(Vector levelPosition, float width, float depth) {
		System.out.println("Init Ai");
		level=new Level(levelPosition, width, depth);
//		for(Car car:cars.values()){
//			Route r=findRoute(car);
//			System.out.println("calced Route: "+r.toString());
//			car.setWayToTarget(r);
//			car.setUpdateFrequenz(UPDATEFREQUENZ);
//		}
		getSender().tell(Message.INITIALIZED, self());
	}
	
	private Route findRoute(Node bot){
		Node nextCoin=findClosestCoin(bot);
		if(nextCoin!=null){
			LevelNode startNode= level.getLevelNode(getNearestNodeinLevel(bot));
			System.out.println("Target Coin: "+nextCoin);
			LevelNode target = level.getLevelNode(getNearestNodeinLevel(nextCoin)); 
			
			LinkedHashMap<LevelNode, AStarNodes> lookAt= new LinkedHashMap<LevelNode, AStarNodes>();
			lookAt.put(startNode, new AStarNodes(startNode.lengthtoNode(target), 0, new LinkedList<LevelNode>()));
			
			List<LevelNode>path =new LinkedList<LevelNode>();
			path.add(startNode);
			
			LinkedList<LevelNode> visit=new LinkedList<LevelNode>();
			
			Route way=aStar(path, lookAt, visit, target);
			lookAt.clear();
			path.clear();
			return way;
		}else{
			return null;
		}
	}
	
	private Route aStar(List<LevelNode> path, LinkedHashMap<LevelNode, AStarNodes> lookAt, List<LevelNode> visited, LevelNode target) {
		if(starParamsValid(path,lookAt, visited, target)){
			for(LevelNode child:path.get(0).getChilds()){
				if(child.getValOfEdge(path.get(0))>0&&!visited.contains(child)){
					int resistance = lookAt.get(path.get(0)).getResistance()+child.getValOfEdge(path.get(0)); //resistance till parent + resistance child to parent
					double distance=child.lengthtoNode(target)+resistance; //pytagoras lenght + resistance
//					System.out.print(" distance: "+distance);
					if(lookAt.containsKey(child))if(lookAt.get(child).getLength()<=distance)continue; //keep only shortest way to child
					lookAt.put(child, new AStarNodes(distance, resistance, path));
				}
			}
			if(path.contains(target))return new Route((int) lookAt.get(target).getLength(), path);
			lookAt.remove(path.get(0));
			visited.add(path.get(0));
			
//			System.out.println(lookAt.toString());
			if(lookAt.isEmpty())return null;
			LevelNode min=Collections.min(lookAt.entrySet(), new Comparator<Map.Entry<LevelNode, AStarNodes>>() {
				@Override
				public int compare(Entry<LevelNode, AStarNodes> o1,
						Entry<LevelNode, AStarNodes> o2) {
					return Double.compare(o1.getValue().getLength(), o2.getValue().getLength());
				}}).getKey();
			
			List<LevelNode> pathMin=new LinkedList<LevelNode>();
			pathMin.add(min);
			pathMin.addAll(lookAt.get(min).getPath());
			if(min.equals(target)){
				//start to move to the next, not to the nearestBase
				pathMin.remove(pathMin.size()-1);
				return new Route((int)lookAt.get(min).getLength(), pathMin);
			}else{
				return aStar(pathMin, lookAt, visited, target);
			}
		}
		System.out.println("Falsche Parameter �bergeben");
		return null;
	}
	
	private boolean starParamsValid(List<LevelNode> path, Map<LevelNode, AStarNodes> lookAt, List<LevelNode> visited, LevelNode target){
		if(path!=null&&lookAt!=null&&!lookAt.isEmpty()&&target!=null&&visited!=null) return true;
		return false;
	}

	private Node findClosestCoin(Node car) {
		float distance = -1;
		Node nearest = null;
		for (Node node : coins.values()) {
			System.out.println("coins: "+node.getId());
//			if (node instanceof Cube) {
//				System.out.println("Node(Cube) findclosest: " + node.id);
				float tempdistance = node.getWorldTransform().getPosition().sub(car.getWorldTransform().getPosition()).length();
				if (tempdistance < distance || distance<0) {
					distance = tempdistance;
//					System.out.println("distance coin: " + distance);
					nearest = node;
				}
//			}
		}
//		VectorImp closestlevelnode = (VectorImp) getNearestNodeinLevel(nearest); //return changed to Node
//		System.out.println("closestlevelNode: " + closestlevelnode);
//		return closestlevelnode;
		return nearest;
	}

	private void aiLoop() {
		System.out.println("Ai Loop");
		int index=0;
		if(!coins.isEmpty()){
			for(Car car:cars.values()){
				if(car.getWayToTarget()==null){
					Route r=findRoute(car);
					System.out.println("calced Route: "+r.toString());
					car.setWayToTarget(r);
					car.setUpdateFrequenz(UPDATEFREQUENZ);
				}
			}
			for(Car car:cars.values()){
				//next position in route(target) sub position
				System.out.println("car: "+car.getId()+" pos: "+car.getPosition()+" "+car);
				System.out.println(index+"next way pos: "+car.getNextWaypoint().getPOS());
				index++;
				if(car.getPosition().equals(car.getNextWaypoint().getPOS())){
					System.out.println("Waypoint reached");
					car.waypointReached();
				}
				if(car.getWayToTarget()!=null){
					Vector vec=car.getVecToNextTarget();
					System.out.println("move direction car: "+vec);
					simulator.tell(new SingelSimulation(car.getId(), SimulateType.TRANSLATEFIX, vec, car.getWorldTransform()), self());
//				if(car.getUpdateFrequenz()==0){
//					car.setWayToTarget(findRoute(car));
//					car.setUpdateFrequenz(UPDATEFREQUENZ);
//				}
				}
			}
		}
		
		getSender().tell(Message.DONE, self());
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (message == Message.LOOP) {
			System.out.println("ai loop");
			getSender().tell(Message.DONE, self());
			aiLoop();
		} else if (message instanceof AiInitialization) {
			AiInitialization init= (AiInitialization) message;
			this.simulator = (init.getSimulator());
			initialize(init.getCenterPosition(), init.getWidth(), init.getDepth());
		} else if (message instanceof NodeCreation) {
			NodeCreation nc=(NodeCreation) message;
			if (nc.type == ObjectTypes.GROUP) {
				nonAiNodes.put(nc.id, nodeFactory.groupNode(nc.id));
			} else if (nc.type == ObjectTypes.CUBE) {
				nonAiNodes.put(nc.id, nodeFactory.cube(nc.id, nc.shader, nc.w, nc.h, nc.d, nc.mass));
			} else if (nc.type == ObjectTypes.PIPE) {
				nonAiNodes.put(nc.id, nodeFactory.pipe(nc.id, nc.shader, nc.r, nc.lats, nc.longs,nc.mass));
			} else if (nc.type == ObjectTypes.SPHERE) {
				nonAiNodes.put(nc.id, nodeFactory.sphere(nc.id, nc.shader, nc.mass));
			} else if (nc.type == ObjectTypes.PLANE) {
				nonAiNodes.put(nc.id, nodeFactory.plane(nc.id, nc.shader, nc.w, nc.d, nc.hight, nc.mass));
			}else if(nc.type == ObjectTypes.OBJECT){
				nonAiNodes.put(nc.id, nodeFactory.obj(nc.id, nc.shader, nc.sourceFile, nc.sourceTex, nc.mass));
			}else if(nc.type == ObjectTypes.CAR){
				Car car=nodeFactory.car(nc.id, nc.shader, nc.sourceFile, nc.speed, nc.mass);
				cars.put(nc.id, car);
			}else if(nc.type == ObjectTypes.COIN){
				coins.put(nc.id, nodeFactory.coin(nc.id, nc.shader, nc.sourceFile, nc.mass));
			}else if(((NodeCreation) message).type == ObjectTypes.CANON){
				Node newNode = nodeFactory.canon(nc.id, nc.shader, nc.sourceFile, nc.sourceTex, nc.mass);
				nonAiNodes.put(newNode.getId(), newNode);
			}
		} else if(message instanceof NodeModification){
			NodeModification nm=(NodeModification) message;
			setNewMatrix(nonAiNodes.get(nm.id), nm);
			setNewMatrix(cars.get(nm.id), nm);
			setNewMatrix(coins.get(nm.id), nm);
		} else if (message instanceof NodeDeletion){
			NodeDeletion delete = (NodeDeletion)message;
			for(String id: delete.ids){
				if(deleteNode(nonAiNodes.get(id), delete))nonAiNodes.remove(id);
				if(deleteNode(cars.get(id), delete))cars.remove(id);
				if(deleteNode(coins.get(id), delete))coins.remove(id);
			}
		}
			
	}
	
	private void setNewMatrix(Node modify, NodeModification nm){
		if(modify!=null && nm.localMod != null){
			modify.updateWorldTransform(nm.localMod);
		}
	}
	
	private boolean deleteNode(Node modify, NodeDeletion nd){
		boolean deleted=false;
		if(modify!=null &&nd != null){
			ArrayList<Edge> removeEdges = new ArrayList<>(); 
			for(Edge e: modify.getEdges()){
				removeEdges.add(e);
			}
			for(Edge e : removeEdges){
				deleted=true;
				modify.removeEdge(e);
			}
		}
		return deleted;
	}
	
	private Vector getNearestNodeinLevel(Node object){
		Vector nearestVec = level.getNearestinLevel(object.getWorldTransform().getPosition());
		//Tagetposition.sub(startPosition)
//		Vector translate=(nearestVec.sub(object.getWorldTransform().getPosition()));
//		if(!translate.equals(new VectorImp(0, 0, 0)))simulator.tell(new SingelSimulation(object.id, SimulateType.TRANSLATE, translate,object.getWorldTransform()), self());
		return nearestVec;
	}

}