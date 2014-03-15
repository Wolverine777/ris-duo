package app;

import static app.nodes.NodeFactory.nodeFactory;
import static vecmath.vecmathimp.FactoryDefault.vecmath;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;

import org.lwjgl.input.Keyboard;

import vecmath.Matrix;
import vecmath.vecmathimp.FactoryDefault;
import vecmath.vecmathimp.VectorImp;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import app.Types.KeyMode;
import app.Types.ObjectTypes;
import app.Types.PhysicType;
import app.Types.SimulateType;
import app.messages.Message;
import app.nodes.GroupNode;
import app.nodes.shapes.*;

/**
 * Put your stuff here
 * 
 * @author Constantin, Benjamin, Fabian
 * 
 */
public class App extends WorldState {

	/*-
	 * 0. Pick shader of choice // TODO 
	 * 1. Create a camera 
	 * 2. Create nodes 
	 * 3. Assign a starting node 
	 * 4. ??? 
	 * 5. Profit!
	 */
	@Override
	protected void initialize() {

		/**
		 * Note: After Creation add keys and physic before transform.
		 */
		setCamera(nodeFactory.camera("Cam"));
		transform(camera, FactoryDefault.vecmath.translationMatrix(0, 0, 10));

		GroupNode head = createGroup("head");
		setStart(head);
		
		announceFloor(floor);
		addPhysicFloor(floor);
		append(floor, head);
		test(head);

	}
	
	private void finalLevel(GroupNode head){
		
	}
	private void test(GroupNode head){
		
		Canon canon = createCanon("Canon", shader, new File("obj/Cannon2.obj"), null, 1.0f);
		simulateOnKey(canon, new HashSet<Integer>(Arrays.asList(Keyboard.KEY_T)), SimulateType.ROTATE, KeyMode.DOWN, new VectorImp(0f, 0f, 1f) ,ObjectTypes.OBJECT);
		simulateOnKey(canon, new HashSet<Integer>(Arrays.asList(Keyboard.KEY_Z)), SimulateType.ROTATE, KeyMode.DOWN, new VectorImp(0f, 1f, 0f) ,ObjectTypes.OBJECT);
		simulateOnKey(canon, new HashSet<Integer>(Arrays.asList(Keyboard.KEY_X)), SimulateType.ROTATE, KeyMode.TOGGLE, new VectorImp(0f, 1f, 0f) ,ObjectTypes.OBJECT);
		transform(canon, vecmath.translationMatrix(2.5f, 0.0f, 3.0f));
		append(canon, head);
		
//		Sphere c4 = createSphere("Shpere!", shader, 1f);
//		transform(c4, vecmath.translationMatrix(-5f, 1f, 0));
//		addPhysic(c4, new VectorImp(0.01f,0.01f,0));
//		append(c4, head);
		
//		Sphere c8 = createSphere("Shpere3", shader, 3f);
//		transform(c8, vecmath.translationMatrix(-5f, 0.5f, 0));
//		addPhysic(c8, new VectorImp(0.0f,0.01f,0));
//		append(c8, head);
		
		
//		Sphere c5 = createSphere("Shpere2", shader, 1f);
//		transform(c5, vecmath.translationMatrix(5f, 3f, 0));
//		addPhysic(c5, new VectorImp(0.0f,0.00f,0));
//		append(c5, head);
		
//		ObjLoader sphere=createObject("objSphere", shader, new File("obj/Sphere.obj"), null, 1f);
//		transform(sphere, vecmath.translationMatrix(4f, 0f, 0f));
//		simulateOnKey(sphere, new HashSet<Integer>(Arrays.asList(Keyboard.KEY_DOWN)), SimulateType.TRANSLATE, KeyMode.DOWN, new VectorImp(0.0f, 0.0f, 0.1f) ,ObjectTypes.CUBE);
//		simulateOnKey(sphere, new HashSet<Integer>(Arrays.asList(Keyboard.KEY_P)), SimulateType.ROTATE, KeyMode.DOWN, new VectorImp(1f, 0, 0) ,ObjectTypes.CUBE);
//		append(sphere, head);
		
		Car car=createCar("Car1", shader, new File("obj/cube.obj"), 0.01f, 1f);
		transform(car, vecmath.scaleMatrix(0.5f, 0.5f, 0.5f));
		transform(car,  vecmath.translationMatrix(-1.0f, floor.getGround(), -1.0f));
		append(car, head);
		
		Coin coin=createCoin("Coin1", shader, new File("obj/cube.obj"), 1f);
		transform(coin, vecmath.scaleMatrix(0.5f, 0.2f, 0.5f));
		transform(coin,  vecmath.translationMatrix(1.0f, floor.getGround(), 0));
		append(coin, head);
		Coin coin2=createCoin("Coin2", shader, new File("obj/cube.obj"), 1f);
		transform(coin2, vecmath.scaleMatrix(0.5f, 0.2f, 0.5f));
		transform(coin2,  vecmath.translationMatrix(-0.5f, floor.getGround(), 0.5f));
		append(coin2, head);
		
		Cube block=createCube("tree", shader, 0.2f,0.4f, 0.2f, 1f);
		append(block, head);

		ObjLoader objsphere=createObject("objSphere2", shader, new File("obj/Sphere.obj"), null, 1f);
		transform(objsphere, vecmath.translationMatrix(4f, 0f, 0f));
		addPhysic(objsphere, new VectorImp(0.0f,-0.01f,0), PhysicType.Collision_only);
		append(objsphere, head);
		
		doCanonBalls();
	}

	public static void main(String[] args) {
		system = ActorSystem.create();
		system.actorOf(Props.create(App.class), "App").tell(Message.INIT,
				ActorRef.noSender());
	}
}