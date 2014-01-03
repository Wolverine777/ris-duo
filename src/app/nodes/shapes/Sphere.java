package app.nodes.shapes;

import static app.nodes.shapes.Vertex.*;
import static app.vecmathimp.FactoryDefault.vecmath;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import app.shader.Shader;
import app.vecmath.Color;
import app.vecmath.Vector;


/**
 * Sphere out of Triangle, generated by subdivision and normalization.
 * 
 * @author Constantin
 * 
 */
public class Sphere extends Shape {

	int divisions = 2;

	float w2 = 0.5f;
	float h2 = 0.5f;
	float d2 = 0.5f;

	private Vector[] p;
	private Color[] c;

	private List<Vector> pL = new ArrayList<Vector>();
	private List<Color> cL = new ArrayList<Color>();
	private List<Vertex> vL = new ArrayList<Vertex>();
	private Vector[] nL;

	private List<Triangle> triangles = new ArrayList<Triangle>();

	/**
	 * Generates a sphere.
	 * 
	 * @param name
	 *            Name
	 *            Shader to use
	 */
	public Sphere(String id, Shader shader) {
		super(id, shader);
		
		mode = GL11.GL_TRIANGLES;

		// Spitze 0
		pL.add(vec(0, h2, 0));
		// Unten 1
		pL.add(vec(0, -h2, 0));
		// Rechts vorne 2
		pL.add(vec(w2, 0, d2));
		// Links hinten 3
		pL.add(vec(-w2, 0, -d2));
		// Rechts hinten 4
		pL.add(vec(w2, 0, -d2));
		// Links vorne 5
		pL.add(vec(-w2, 0, d2));
		p = pL.toArray(new Vector[pL.size()]);

		// Colors
		cL.add(col(1, 0.5f, 1));
		cL.add(col(1, 0.5f, 1));
		cL.add(col(1, 0.5f, 1));
		cL.add(col(1, 0.5f, 1));
		cL.add(col(1, 0.5f, 1));
		cL.add(col(1, 0.5f, 1));
		
//		cL.add(col(.1f, .1f, .1f));
//		cL.add(col(0, 0.1f, 0.8f));
//		cL.add(col(0.2f, 0, 1));
//		cL.add(col(1, 0.5f, 1));
//		cL.add(col(0.7f, 0.1f, 0.3f));
//		cL.add(col(0.7f, 0.6f, 0.3f));
		c = cL.toArray(new Color[cL.size()]);

		nL = setNull();
		calculateSurfaceNormal();
		// front top
		triangles.add(new Triangle(v(p[0], c[0]), v(p[2], c[2]), v(p[5], c[5])));
		// front bottom
		triangles.add(new Triangle(v(p[1], c[1]), v(p[2], c[2]), v(p[5], c[5])));
		// left top
		triangles.add(new Triangle(v(p[0], c[0]), v(p[5], c[5]), v(p[3], c[3])));
		// left bottom
		triangles.add(new Triangle(v(p[1], c[1]), v(p[5], c[5]), v(p[3], c[3])));
		// back top
		triangles.add(new Triangle(v(p[0], c[0]), v(p[4], c[4]), v(p[3], c[3])));
		// back bottom
		triangles.add(new Triangle(v(p[1], c[1]), v(p[4], c[4]), v(p[3], c[3])));
		// right top
		triangles.add(new Triangle(v(p[0], c[0]), v(p[4], c[4]), v(p[2], c[2])));
		// right bottom
		triangles.add(new Triangle(v(p[1], c[1]), v(p[4], c[4]), v(p[2], c[2])));

		// Subdivision
		for (int i = 0; i < divisions; i++) {
			List<Triangle> newTris = new ArrayList<Triangle>();
			for (Triangle t : triangles) {
				newTris.addAll(subdivide(t));
			}
			triangles = newTris;
		}

		// Normalization
		for (Triangle t : triangles) {
			normalize(t);
		}

		// Output
		for (Triangle t : triangles) {
			vL.addAll(t.out());
		}
		vertices = vL.toArray(new Vertex[vL.size()]);
		positionData = BufferUtils.createFloatBuffer(vertices.length * vecmath.vectorSize());
		colorData = BufferUtils.createFloatBuffer(vertices.length * vecmath.vectorSize());
		normalData = BufferUtils.createFloatBuffer(vertices.length * vecmath.vectorSize());

		for (Vertex v : vertices) {
			positionData.put(v.position.asArray());
			colorData.put(v.color.asArray());
		}
		for (int i = 0; i < pL.size(); i++) {
			normalData.put(nL[i].asArray());
		}
		positionData.rewind();
		colorData.rewind();
		normalData.rewind();
	}

	// Auxilliary class to represent one triangle
	private class Triangle {
		Vertex a;
		Vertex b;
		Vertex c;

		Triangle(Vertex first, Vertex second, Vertex third) {
			a = first;
			b = second;
			c = third;
		}

		public List<Vertex> out() {
			return new ArrayList<Vertex>() {
				private static final long serialVersionUID = 1L;
				{
					add(a);
					add(b);
					add(c);
				}
			};
		}
	}


	// Make construction of normals easy on the eyes.
	private Vector norm(float nx, float ny, float nz) {
		return vecmath.vector(nx, ny, nz);
	}

	private List<Triangle> subdivide(Triangle tri) {

		List<Triangle> outTris = new ArrayList<Triangle>();
		/*-
		 *       a			       a
		 *       /\			       /\
		 *      /  \		      /  \
		 *     /    \	  --\   d/____\e
		 *    /      \	  --/	/\    /\ 
		 *   / 	 	  \        /  \  /  \
		 * b/__________\c	 b/____\/____\c
		 * 						   f
		 */

		Vertex a = tri.a;
		Vertex b = tri.b;
		Vertex c = tri.c;

		Vertex d;
		Float d1;
		Float d2;
		Float d3;

		Vertex e;
		Float e1;
		Float e2;
		Float e3;

		Vertex f;
		Float f1;
		Float f2;
		Float f3;

		d1 = (a.position.x() + b.position.x()) / 2;
		d2 = (a.position.y() + b.position.y()) / 2;
		d3 = (a.position.z() + b.position.z()) / 2;
//		d = v(vec(d1, d2, d3), col((float) Math.random(), (float) Math.random(), (float) Math.random()));
		d = v(vec(d1, d2, d3), col((float) Math.random(), 0.6f, 1));


		e1 = (a.position.x() + c.position.x()) / 2;
		e2 = (a.position.y() + c.position.y()) / 2;
		e3 = (a.position.z() + c.position.z()) / 2;
//		e = v(vec(e1, e2, e3), col((float) Math.random(), (float) Math.random(), (float) Math.random()));
		e = v(vec(e1, e2, e3), col((float) Math.random(), 0.6f, 1));


		f1 = (b.position.x() + c.position.x()) / 2;
		f2 = (b.position.y() + c.position.y()) / 2;
		f3 = (b.position.z() + c.position.z()) / 2;
//		f = v(vec(f1, f2, f3), col((float) Math.random(), (float) Math.random(), (float) Math.random()));
		f = v(vec(f1, f2, f3), col((float) Math.random(), 0.6f, 1));


		outTris.add(new Triangle(a, d, e));
		outTris.add(new Triangle(d, b, f));
		outTris.add(new Triangle(d, e, f));
		outTris.add(new Triangle(e, f, c));

		return outTris;
	}

	private void normalize(Triangle tri) {
		tri.a.newPos(tri.a.position.normalize());
		tri.b.newPos(tri.b.position.normalize());
		tri.c.newPos(tri.c.position.normalize());
	}

	private Vector[] setNull() {
		Vector[] ntemp = new Vector[pL.size()];
		for (int i = 0; i < pL.size(); i++) {
			ntemp[i] = norm(0, 0, 0);
		}
		return ntemp;
	};


	public void calculateSurfaceNormal() {
		Vector finalNormal = norm(0, 0, 0);
		for (int i = 0; i < pL.size() - 3; i += 3) {
			Vector v0 = pL.get(i);
			Vector v1 = pL.get(i + 1);
			Vector v2 = pL.get(i + 2);
			Vector normal = (v1.sub(v0).cross(v2.sub(v0))).normalize();
			nL[i] = nL[i].add(normal);
			nL[i + 1] = nL[i + 1].add(normal);
			nL[i + 2] = nL[i + 2].add(normal);

			System.out.println(normal);

		}
		Vector[] tempNL = nL;
		ArrayList<Vector> temp = new ArrayList<Vector>();
		ArrayList<Integer> index = new ArrayList<Integer>();
		for (int i = 0; i < pL.size(); i++) {
			temp.clear();
			index.clear();
			finalNormal = norm(0, 0, 0);
			if (pL.get(i) != null) {
				index.add(i);
				temp.add(pL.get(i));
				for (int j = i + 1; j < pL.size(); j++) {
					if (pL.get(i).equals(pL.get(j)) && pL.get(j) != null) {
						temp.add(pL.get(j));
						tempNL[j] = null;
						index.add(j);
					}
				}
			}
			for (int k = 0; k < temp.size(); k++) {
				System.out.println("k" + k + " " + temp.get(k));

				finalNormal = finalNormal.add(temp.get(k));
				System.out.println("finalN" + temp.get(k));
			}
			finalNormal = finalNormal.normalize();
			for (int in : index) {
				// System.out.println(in);
				nL[in] = finalNormal;
				System.out.println("final" + finalNormal);
			}
		}
	}
}