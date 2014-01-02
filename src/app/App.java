package app;

import static app.vecmathimp.FactoryDefault.vecmath;
import static app.nodes.NodeFactory.nodeFactory;

import java.io.File;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import app.messages.Message;
import app.nodes.GroupNode;
import app.nodes.shapes.Cube;
import app.shader.Shader;
import app.vecmathimp.FactoryDefault;

/**
 * Put your stuff here
 * 
 * @author Constantin
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

		setCamera(nodeFactory.camera("Cam"));
		transform(camera, FactoryDefault.vecmath.translationMatrix(0, 0, 3));

		GroupNode head = createGroup("Group");
		setStart(head);

		System.out.println("Using shader " + shader);

		Cube c1 = createCube("Cube1", shader, 0.3f, 0.3f, 0.3f);
		append(c1, head);

		Cube c2 = createCube("Cube2", shader, 1.5f, 1.5f, 1.5f);
		transform(c2, vecmath.translationMatrix(1, 0, 0));
		append(c2, head);

		Cube c3 = createCube("Cube3", shader, .8f, 1.2f, 1);
		transform(c3, vecmath.translationMatrix(-1, -1, 0));
		append(c3, head);

	}

	public static void main(String[] args) {
		system = ActorSystem.create();
		system.actorOf(Props.create(App.class), "App").tell(Message.INIT,
				ActorRef.noSender());
	}
}