package app.nodes.shapes;

import static app.nodes.shapes.Vertex.*;
import static vecmath.vecmathimp.FactoryDefault.vecmath;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import vecmath.Color;
import vecmath.Matrix;
import vecmath.Vector;
import vecmath.vecmathimp.MatrixImp;
import app.shader.Shader;


/**
 * Sphere out of Triangle, generated by subdivision and normalization.
 * 
 * @author Constantin optimized by Benjamin
 * 
 */
public class Sphere extends Shape {

	//higher makes more Triangels
	private final int DIVISIONS = 1;

	float w2 = 0.5f;
	float h2 = 0.5f;
	float d2 = 0.5f;

	private Vector[] p = {
			// Spitze 0
			vec(0, h2, 0),
			// Unten 1
			vec(0, -h2, 0),
			// Rechts vorne 2
			vec(w2, 0, d2),
			// Links hinten 3
			vec(-w2, 0, -d2),
			// Rechts hinten 4
			vec(w2, 0, -d2),
			// Links vorne 5
			vec(-w2, 0, d2)};
			
	
	//TargetVec-StartVec.cros(TragetVec-StartVec)
//	private Vector[] n={
//			//for front top
//			p[5].sub(p[0]).cross(p[2].sub(p[0])),
//			//for bottom front
//			p[2].sub(p[1]).cross(p[5].sub(p[1])),
//			//left top
//			p[3].sub(p[0]).cross(p[5].sub(p[0])),
//			//left bottom
//			p[5].sub(p[1]).cross(p[3].sub(p[1])),
//			// back top
//			p[3].sub(p[0]).cross(p[4].sub(p[0])),
//			// back bottom
//			p[3].sub(p[1].cross(p[4].sub(p[1]))),
//			// right top
//			p[2].sub(p[0]).cross(p[4].sub(p[0])),
//			// right bottom
//			p[4].sub(p[1]).cross(p[2].sub(p[1]))};
	
	private List<Triangle> triangles = new ArrayList<Triangle>();

	/**
	 * Generates a sphere.
	 * 
	 * @param name
	 *            Name
	 *            Shader to use
	 */
	public Sphere(String id, Shader shader, float mass, Matrix modelMatrix) {
		super(id, shader, mass, modelMatrix);
		mode = GL11.GL_TRIANGLES;

//		// Spitze 0
//		pL.add(vec(0, h2, 0));
//		// Unten 1
//		pL.add(vec(0, -h2, 0));
//		// Rechts vorne 2
//		pL.add(vec(w2, 0, d2));
//		// Links hinten 3
//		pL.add(vec(-w2, 0, -d2));
//		// Rechts hinten 4
//		pL.add(vec(w2, 0, -d2));
//		// Links vorne 5
//		pL.add(vec(-w2, 0, d2));
//		p = pL.toArray(new Vector[pL.size()]);

		// Colors
//		cL.add(col(.1f, .1f, .1f));
//		cL.add(col(0, 0.1f, 0.8f));
//		cL.add(col(0.2f, 0, 1));
//		cL.add(col(1, 0.5f, 1));
//		cL.add(col(0.7f, 0.1f, 0.3f));
//		cL.add(col(0.7f, 0.6f, 0.3f));
		Color[] c = {col(1, 0.5f, 1),col(1, 0.5f, 1),col(1, 0.5f, 1),col(1, 0.5f, 1),col(1, 0.5f, 1),col(1, 0.5f, 1)};

		Vector[] normals = setNull();
		//TODO:suck find better
		calculateSurfaceNormal(normals);
//		// front top
//		triangles.add(new Triangle(v(p[0], c[0], n[0]), v(p[2], c[2], n[0]), v(p[5], c[5], n[0])));
//		// front bottom
//		triangles.add(new Triangle(v(p[1], c[1], n[1]), v(p[2], c[2], n[1]), v(p[5], c[5], n[1])));
//		// left top
//		triangles.add(new Triangle(v(p[0], c[0], n[2]), v(p[5], c[5], n[2]), v(p[3], c[3], n[2])));
//		// left bottom
//		triangles.add(new Triangle(v(p[1], c[1], n[3]), v(p[5], c[5], n[3]), v(p[3], c[3], n[3])));
//		// back top
//		triangles.add(new Triangle(v(p[0], c[0], n[4]), v(p[4], c[4], n[4]), v(p[3], c[3], n[4])));
//		// back bottom
//		triangles.add(new Triangle(v(p[1], c[1], n[5]), v(p[4], c[4], n[5]), v(p[3], c[3], n[5])));
//		// right top
//		triangles.add(new Triangle(v(p[0], c[0], n[6]), v(p[4], c[4], n[6]), v(p[2], c[2], n[6])));
//		// right bottom
//		triangles.add(new Triangle(v(p[1], c[1], n[7]), v(p[4], c[4], n[7]), v(p[2], c[2], n[7])));

		triangles.add(new Triangle(v(p[0], c[0], n(p[0], p[5], p[2])), v(p[2], c[2], n(p[2], p[0], p[5])), v(p[5], c[5], n(p[5], p[2], p[0]))));
		triangles.add(new Triangle(v(p[1], c[1], n(p[1], p[2], p[5])), v(p[2], c[2], n(p[2], p[5], p[1])), v(p[5], c[5], n(p[5], p[1], p[2]))));
		triangles.add(new Triangle(v(p[0], c[0], n(p[0], p[3], p[5])), v(p[5], c[5], n(p[5], p[0], p[3])), v(p[3], c[3], n(p[3], p[5], p[0]))));
		triangles.add(new Triangle(v(p[1], c[1], n(p[1], p[5], p[3])), v(p[5], c[5], n(p[5], p[3], p[1])), v(p[3], c[3], n(p[3], p[1], p[5]))));
		triangles.add(new Triangle(v(p[0], c[0], n(p[0], p[4], p[3])), v(p[4], c[4], n(p[4], p[3], p[0])), v(p[3], c[3], n(p[3], p[0], p[4]))));
		triangles.add(new Triangle(v(p[1], c[1], n(p[1], p[3], p[4])), v(p[4], c[4], n(p[4], p[1], p[3])), v(p[3], c[3], n(p[3], p[4], p[1]))));
		triangles.add(new Triangle(v(p[0], c[0], n(p[0], p[2], p[4])), v(p[4], c[4], n(p[4], p[0], p[2])), v(p[2], c[2], n(p[2], p[4], p[0]))));
		triangles.add(new Triangle(v(p[1], c[1], n(p[1], p[4], p[2])), v(p[4], c[4], n(p[4], p[2], p[1])), v(p[2], c[2], n(p[2], p[1], p[4]))));
		// Subdivision
		for (int i = 0; i < DIVISIONS; i++) {
			List<Triangle> newTris = new ArrayList<Triangle>();
			for (Triangle t : triangles) {
				newTris.addAll(subdivide(t));
			}
			triangles = newTris;
		}

//		for(Triangle t:triangles){
//			t.a.setNormal(n(t.a.position, t.b.position, t.c.position));
//			t.b.setNormal(n(t.b.position, t.c.position, t.a.position));
//			t.c.setNormal(n(t.c.position, t.a.position, t.b.position));
//		}
		// Normalization
		for (Triangle t : triangles) {
			normalize(t);
		}

		List<Vertex> vL = new ArrayList<Vertex>();
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
//			normalData.put(v.normal.asArray());
		}
		for (int i = 0; i < p.length; i++) {
			normalData.put(normals[i].asArray());
		}
		positionData.rewind();
		colorData.rewind();
		normalData.rewind();
		findCenter();
		if(MatrixImp.isTranslationMatrix(modelMatrix))setCenter(modelMatrix.mult(vecmath.translationMatrix(getCenter())).getPosition());
		if(modelMatrix.get(0, 0) == modelMatrix.get(1, 1) && modelMatrix.get(1, 1) == modelMatrix.get(2, 2))radius = modelMatrix.get(0, 0)*radius;
	}
	
	private Vector n(Vector start, Vector targetR, Vector targetL){
		Vector a=targetL.sub(start);
		Vector b=targetR.sub(start);
//		return targetR.sub(start).cross(targetL.sub(start));
		return a.cross(b).mult((float)(1/(Math.sqrt(Math.pow(a.length(),2) * Math.pow(b.length(), 2) - Math.pow(a.mult(b).length(),2) ))));
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
		Vector dPos=vec(d1, d2, d3);

		e1 = (a.position.x() + c.position.x()) / 2;
		e2 = (a.position.y() + c.position.y()) / 2;
		e3 = (a.position.z() + c.position.z()) / 2;
//		e = v(vec(e1, e2, e3), col((float) Math.random(), (float) Math.random(), (float) Math.random()));
		Vector ePos=vec(e1, e2, e3);

		f1 = (b.position.x() + c.position.x()) / 2;
		f2 = (b.position.y() + c.position.y()) / 2;
		f3 = (b.position.z() + c.position.z()) / 2;
//		f = v(vec(f1, f2, f3), col((float) Math.random(), (float) Math.random(), (float) Math.random()));
		Vector fPos=vec(f1, f2, f3);
		
		d = v(dPos, col((float) Math.random(), 0.6f, 1));
		e = v(ePos, col((float) Math.random(), 0.6f, 1));
		f = v(fPos, col((float) Math.random(), 0.6f, 1));


		//TargetVec-StartVec.cros(TragetVec-StartVec)
//		Vector norm[]={
//				ePos.sub(a.position).cross(dPos.sub(a.position)),
//				dPos.sub(a.position).cross(ePos.sub(a.position)),
//				b.position.sub(dPos).cross(fPos.sub(dPos)),
//				fPos.sub(dPos).cross(b.position.sub(dPos)),
//				fPos.sub(dPos).cross(ePos.sub(dPos)),
//				ePos.sub(dPos).cross(fPos.sub(dPos)),
//				fPos.sub(ePos).cross(c.position.sub(ePos))
//				c.position.sub(ePos).cross(fPos.sub(ePos))
//		};
		
//		outTris.add(new Triangle(a, d, e));
//		outTris.add(new Triangle(d, b, f));
//		outTris.add(new Triangle(d, e, f));
//		outTris.add(new Triangle(e, f, c));
		outTris.add(new Triangle(v(a.position, a.color, n(a.position, dPos, ePos)), v(d.position, d.color, n(dPos, ePos, a.position)), v(e.position, e.color, n(ePos, a.position, dPos))));
		outTris.add(new Triangle(v(d.position, d.color, n(dPos, b.position, fPos)), v(b.position, b.color, n(b.position, fPos, dPos)), v(f.position, f.color, n(fPos, dPos, b.position))));
		outTris.add(new Triangle(v(d.position, d.color, n(dPos, fPos, ePos)), v(e.position, e.color, n(ePos, dPos, fPos)), v(f.position, f.color, n(fPos, ePos, dPos))));
		outTris.add(new Triangle(v(e.position, e.color, n(ePos, fPos, c.position)), v(f.position, f.color, n(fPos, c.position, ePos)), v(c.position, c.color, n(c.position, ePos, fPos))));

		return outTris;
	}

	private void normalize(Triangle tri) {
		tri.a.newPos(tri.a.position.normalize());
		tri.b.newPos(tri.b.position.normalize());
		tri.c.newPos(tri.c.position.normalize());
	}

	private Vector[] setNull() {
		Vector[] ntemp = new Vector[p.length];
		for (int i = 0; i < p.length; i++) {
			ntemp[i] = norm(0, 0, 0);
		}
		return ntemp;
	}

	//this totally suck, who does this?
	private void calculateSurfaceNormal(Vector[] nL) {
		Vector finalNormal = norm(0, 0, 0);
		for (int i = 0; i < p.length - 3; i += 3) {
			Vector v0 = p[i];
			Vector v1 = p[i + 1];
			Vector v2 = p[i + 2];
			Vector normal = (v1.sub(v0).cross(v2.sub(v0))).normalize();
			nL[i] = nL[i].add(normal); //was zum teufel...  TODO: fragen was add bei Vector macht
			nL[i + 1] = nL[i + 1].add(normal);
			nL[i + 2] = nL[i + 2].add(normal);
		}
//		Vector[] tempNL = nL;
		ArrayList<Vector> temp = new ArrayList<Vector>();
		ArrayList<Integer> index = new ArrayList<Integer>();
		for (int i = 0; i < p.length; i++) {
			temp.clear();
			index.clear();
			finalNormal = norm(0, 0, 0);
			if (p[i] != null) {
				index.add(i);
				temp.add(p[i]);
				for (int j = i + 1; j < p.length; j++) {
					if (p[i].equals(p[j]) && p[j] != null) {
						temp.add(p[j]);
//						tempNL[j] = null; //why? only usage
						nL[j] = null;
						index.add(j);
					}
				}
			}
			for (int k = 0; k < temp.size(); k++) {
				finalNormal = finalNormal.add(temp.get(k));
				
			}
			finalNormal = finalNormal.normalize();
			for (int in : index) {
				nL[in] = finalNormal;
			}
		}
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


	@Override
	public Shape clone() {
		return new Sphere(new String(getId()), shader, DIVISIONS, getWorldTransform());
	}
}