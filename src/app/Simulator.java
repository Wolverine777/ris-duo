package app;


import static app.nodes.NodeFactory.nodeFactory;
import static vecmath.vecmathimp.FactoryDefault.vecmath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import vecmath.Matrix;
import vecmath.Vector;
import vecmath.vecmathimp.MatrixImp;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import akka.actor.UntypedActor;
import app.Types.KeyMode;
import app.Types.ObjectTypes;
import app.Types.SimulateType;
import app.datatype.KeyDef;
import app.edges.Edge;
import app.eventsystem.CameraCreation;
import app.eventsystem.NodeCreation;
import app.eventsystem.NodeDeletion;
import app.eventsystem.NodeModification;
import app.eventsystem.PhysicModification;
import app.eventsystem.SimulateCreation;
import app.messages.KeyState;
import app.messages.Message;
import app.messages.SingelSimulation;
import app.nodes.Node;
import app.nodes.shapes.Shape;
import app.toolkit.StopWatch;

public class Simulator extends UntypedActor {
    
    private Map<String, Node> nodes = new HashMap<String, Node>();
    private SetMultimap<Node, KeyDef> simulations=HashMultimap.create();
    private Set<Integer> pressedKeys = new HashSet<Integer>();
    private Set<Integer> toggeled=new HashSet<Integer>();
	private float angle = 0;
	private StopWatch sw=new StopWatch();
	private float elapsed=0;
    
    private void initialize() {
        getSender().tell(Message.INITIALIZED, self());
    }

    private void simulate() throws Exception {
    	elapsed=sw.elapsed();
    	for(Map.Entry<Node, KeyDef> entry:simulations.entries()){
    		Set<Integer> keys=entry.getValue().getKeys();
//    		if(entry.getValue().getType()!=SimulateType.PHYSIC){
    			if(keys==null||keys.isEmpty()){
    				doSimulation(entry.getKey(), entry.getValue().getType(), entry.getValue().getVector());
    			}else{
    				if(entry.getValue().getMode()==KeyMode.DOWN){
    					boolean contains=false;
    					for(Integer i:keys)if(pressedKeys.contains(i))contains=true;
    					if(contains)doSimulation(entry.getKey(), entry.getValue().getType(), entry.getValue().getVector());
    				}else if(entry.getValue().getMode()==KeyMode.TOGGLE){
    					boolean contains=false;
    					for(Integer i:keys)if(toggeled.contains(i))contains=true;
    					if(contains)doSimulation(entry.getKey(), entry.getValue().getType(), entry.getValue().getVector());
    				}else{
    					throw new Exception("Add Key Mode!");
    				}
    			}
//    		}
    	}
                // m�glich: hier alle modifizierten schicken, nicht mehr n�tig da modify matrix getellt
        getSender().tell(Message.DONE, self());
    }
    
    private void doSimulation(Node node, SimulateType type, Vector vec){
    	if(type==SimulateType.ROTATE){
    		angle += elapsed *(vec.length()*90);
//    		angle= 0.5f;
//    		node.setLocalTransform(vecmath.rotationMatrix(vec.x(), vec.y(),vec.z(), angle));
//    		node.updateWorldTransform();
    		Vector v=node.getWorldTransform().getPosition();
    		
    		Matrix modify=MatrixImp.translate(v.x(),v.y(),v.z()).mult(vecmath.rotationMatrix(vec.x(), vec.y(),vec.z(), angle).mult(MatrixImp.translate(-v.x(),-v.y(),-v.z())));
    		node.updateWorldTransform(modify);
			angle = 0;
			getSender().tell(new NodeModification(node.id,modify), self());
			//m�glich: alle matritzen f�r einen object aufmultiplizeiren und dann schicken. 
    	}
    	else if(type==SimulateType.TRANSLATE){
//    		node.setLocalTransform(MatrixImp.translate(vec));
//    		node.updateWorldTransform();
    		Matrix modify=MatrixImp.translate(vec);
    		node.updateWorldTransform(modify);
    		getSender().tell(new NodeModification(node.id,/*node.getWorldTransform()*/modify), self());
    	}
    	else if(type==SimulateType.PHYSIC){
    		if(node.force != null){
//    			node.setLocalTransform(MatrixImp.translate(vec));
//    			node.updateWorldTransform();
    		Matrix modify=MatrixImp.translate(node.force.mult((elapsed*60)));
    		node.updateWorldTransform(modify);
    		getSender().tell(new NodeModification(node.id,modify), self());
    		node.force=null;
    	    }
    	}
    	//st end nodemodification
    }
    
    @Override
    public void onReceive(Object message) throws Exception {
        if (message == Message.LOOP) {
        	System.out.println("simulation loop");
            simulate();
        } else if (message == Message.INIT) {
            initialize();
        } else if(message instanceof KeyState){
        	pressedKeys.clear();
        	toggeled.clear();
        	pressedKeys.addAll(((KeyState)message).getPressedKeys());
        	toggeled.addAll(((KeyState)message).getToggled());
        }else if (message instanceof NodeModification) {
        	System.out.println("Nodes " + nodes);
        	System.out.println("Accesing " + ((NodeModification) message).id);
        	if(nodes.containsKey(((NodeModification) message).id)){
        		Node modify = nodes.get(((NodeModification) message).id);
        		if (((NodeModification) message).localMod != null) {
//        			 modify.setLocalTransform(((NodeModification) message).localMod);
//    				 modify.updateWorldTransform();
    				 modify.updateWorldTransform(((NodeModification) message).localMod);
        		}
//        		if (((NodeModification) message).appendTo != null) {
//        			modify.appendTo(nodes.get(((NodeModification) message).appendTo));
//        		}//cause error on run, delets simulations
        	}
        } 
//        else if (message instanceof StartNodeModification) {
//        	Node start = nodes.get(((StartNodeModification) message).id);
//        }
        else if(message instanceof SimulateCreation){
        	SimulateCreation sc=(SimulateCreation)message;
        	Node newNode=null;
        	if(!nodes.containsKey(sc.id)){
        		System.out.println("jashdlhwidaljhdlahs"+sc.id);
        		//TODO: ein Type reicht nur ein Shape, von den objekten wird nur id und woldtrafo benoetigt.
        		//TODO: Generics?
        		if (((NodeCreation) message).type == ObjectTypes.GROUP) {
        			newNode = nodeFactory.groupNode(((NodeCreation) message).id);
        			nodes.put(newNode.id, newNode);
        		} else if (((NodeCreation) message).type == ObjectTypes.CUBE) {
        			newNode = nodeFactory.cube(((NodeCreation) message).id, ((NodeCreation) message).shader, ((NodeCreation) message).w, ((NodeCreation) message).h,
    						((NodeCreation) message).d, ((NodeCreation) message).mass);
        			nodes.put(newNode.id, newNode);
        		} else if (((NodeCreation) message).type == ObjectTypes.SPHERE) {
    				newNode = nodeFactory.sphere(((NodeCreation) message).id,
    						((NodeCreation) message).shader, ((NodeCreation) message).mass);
    				nodes.put(newNode.id, newNode);
    			} else if(((NodeCreation) message).type == ObjectTypes.CAMERA){
        			newNode = nodeFactory.camera(((CameraCreation) message).id);
        			nodes.put(((CameraCreation) message).id, newNode);
        		}else if(((NodeCreation) message).type == ObjectTypes.OBJECT){
    				NodeCreation nc=(NodeCreation) message;
    				newNode = nodeFactory.obj(nc.id, nc.shader, nc.sourceFile, nc.sourceTex, nc.mass);
//    				System.out.println("radius objtest simulator: " + ((Shape) newNode).getRadius());
    				nodes.put(newNode.id, newNode);
        		} 
        		
        		else{
        			throw new Exception("Please implement Type");
        		}
        	}
        	newNode=nodes.get(sc.id);
        	if(sc.getSimulation()!=SimulateType.NONE){
//        		System.out.println("next simulation" + simulations.toString());
//        		System.out.println("haaaaaaaaaaaaaaaaaaaaaaaaaaaaaalllllllllllllllllllooooooooo\n"+newNode.id+sc.getSimulation()+"\n"+"local\n"+newNode.getLocalTransform()+"world\n"+newNode.getWorldTransform()+"keys"+sc.getKeys());
        		simulations.put(newNode, new KeyDef(sc.getSimulation(), sc.getKeys(), sc.getMode(), sc.getVector()));
//        		System.out.println("last simulation" + simulations.toString());
        		newNode.setLocalTransform(sc.modelmatrix);
        		newNode.updateWorldTransform(); //TODO: Node klasse fixen.... was geht denn hier
//        		System.out.println("simulations\n"+simulations.get(newNode).getVector()+"\n"+simulations.isEmpty()+sc.getSimulation());
        		
        	}else{
//        		simulations.remove(newNode);
        		for(KeyDef kd:simulations.get(newNode)){
        			if(kd.getKeys().containsAll(sc.getKeys())&&kd.getMode()==sc.getMode()){
        				simulations.remove(newNode, kd);
        			}
        		}
        	}
        }
        else if (message instanceof PhysicModification) {
        	if (nodes.containsKey(((PhysicModification) message).id)){
        		Node modify = nodes.get(((PhysicModification) message).id);
        		modify.setForce((((PhysicModification) message)).force);
        		for(KeyDef k :simulations.get(modify)){
        		  if(k.getType().equals(SimulateType.PHYSIC)){
        			  k.setVector(modify.getForce());
//        			  doSimulation(modify, k.getType(), k.getVector());
        		  }
        		}
        	}
        }else if (message instanceof SingelSimulation){
        	SingelSimulation simulation=(SingelSimulation)message;
        	if(nodes.containsKey(simulation.getNodeId()))doSimulation(nodes.get(simulation.getNodeId()), simulation.getType(), simulation.getVec());
        	else{
        		nodes.put(simulation.getNodeId(), nodeFactory.groupNode(simulation.getNodeId(), simulation.getModelMatrix()));
        		doSimulation(nodes.get(simulation.getNodeId()), simulation.getType(), simulation.getVec());
        		nodes.remove(simulation.getNodeId());
        	}
        }  else if (message instanceof NodeDeletion){
        	NodeDeletion delete = (NodeDeletion)message;
			for(String id: delete.ids){
				Node modify = nodes.get(id);
				ArrayList<Edge> removeEdges = new ArrayList<>(); 
				if(modify!=null){
				for(Edge e: modify.getEdges()){
					removeEdges.add(e);
					nodes.get(e.getOtherNode(modify).id).removeEdge(e);
					
				}
				for(Edge e : removeEdges){
					modify.removeEdge(e);
				}
			
				nodes.remove(modify);
				}
			}
		}
    }
}