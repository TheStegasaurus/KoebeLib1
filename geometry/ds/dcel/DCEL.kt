package geometry.ds.dcel

import java.util.ArrayList;
import geometry.ds.dcel.DCEL.Face



/**
 * Created by John C. Bowers on 5/13/17.
 */
class DCEL<VertexData, EdgeData, FaceData>(outerFaceData: FaceData? = null) {

    val verts = mutableListOf<Vertex>()
    val darts = mutableListOf<Dart>()
    val edges = mutableListOf<Edge>()
    val faces = mutableListOf<Face>()

    val outerFace: Face?

    init {
        outerFace = if (outerFaceData != null) Face(data = outerFaceData) else null
    }

    inner class Vertex(var aDart: Dart? = null, var data : VertexData) {

        init {
            verts.add(this)
        }

        /**
         * Returns a counter-clockwise list of the outgoing darts from an arbitrary starting point.
         */
        fun edges(): List<Edge> {
            return outDarts().map {
                val edge = it.edge
                if (edge != null) edge else throw MalformedDCELException("Dart has null edge.")
            }
        }

        /**
         * Returns a counter-clockwise list of the outgoing darts from an arbitrary starting point.
         */
        fun outDarts(): List<Dart> {
            val darts = mutableListOf<Dart>()

            tailrec fun collectOutDarts(curr: Dart, orig: Dart) {
                darts.add(curr)
                val next = curr.prev?.twin
                if (next == null) {
                    throw MalformedDCELException("A dart has a null twin pointer.")
                } else if (next != orig) {
                    collectOutDarts(next, orig)
                }
            }

            val startDart = this.aDart

            if (startDart != null) {
                collectOutDarts(startDart, startDart)
                return darts
            }  else {
                throw MalformedDCELException("Vertex has a null outgoing dart (.aDart).")
            }
        }

        /**
         * Returns a counter-clockwise list of the incoming darts from an arbitrary starting point.
         */
        fun inDarts(): List<Dart> {
            val darts = mutableListOf<Dart>()

            tailrec fun collectInDarts(curr: Dart, orig: Dart) {
                darts.add(curr)
                val next = curr.twin?.prev
                if (next == null) {
                    throw MalformedDCELException("A dart has a null prev pointer.")
                } else if (next != orig) {
                    collectInDarts(next, orig)
                }
            }

            val startDart = this.aDart?.twin

            if (startDart != null) {
                collectInDarts(startDart, startDart)
                return darts
            }  else {
                throw MalformedDCELException("Vertex has a null outgoing dart or its outgoing dart has a null twin.")
            }
        }
    }

    inner class Dart(
                     var edge: Edge? = null,
                     var origin: Vertex? = null,
                     var face: Face? = null,

                     var prev: Dart? = null,
                     var next: Dart? = null,
                     var twin: Dart? = null
                    )
    {
        init {
            darts.add(this)

            edge?.aDart = this
            origin?.aDart = this
            face?.aDart = this

            prev?.next = this
            next?.prev = this
            twin?.twin = this
        }

        val dest: Vertex? get() = twin?.origin

        /**
         * Convenience method to make this the prev of newNext (sets two pointers)
         */
        fun makeNext(newNext: Dart) {
            next = newNext
            newNext.prev = this
        }

        /**
         * Convenience method to make this a twin of newTwin (sets two pointers)
         */
        fun makeTwin(newTwin: Dart?) {
            twin = newTwin
            if (newTwin != null)
                newTwin.twin = this
        }

        /**
         * Convenience method to make this the next of newPrev (sets two pointers)
         */
        fun makePrev(newPrev: Dart) {
            prev = newPrev
            newPrev.next = this
        }

        /**
         * Get the cycle of edges starting from this (follows next pointers)
         */
        fun cycle(): List<Dart> {
            val cycle = mutableListOf<Dart>()

            tailrec fun collectCycle(curr: Dart, orig: Dart) {
                cycle.add(curr)
                val next = curr.next
                if (next == null) {
                    throw MalformedDCELException("A dart has a null next pointer.")
                } else if (next != orig) {
                    collectCycle(next, orig)
                }
            }

            collectCycle(this, this)
            return cycle
        }

        /**
         * Get the cycle of edges starting from this (follows prev pointers)
         */
        fun reverse_cycle(): List<Dart> {
            return cycle().reversed()
        }
    }

    inner class Edge(var aDart: Dart? = null, var data: EdgeData) {

        init {
            edges.add(this)
        }

    }

    inner class Face(var aDart: Dart? = null, var data: FaceData) {

        init {
            faces.add(this)
        }

        /**
         * @return The cycle of dart incident this face.
         */
        fun darts(): List<Dart> {
            val retDart = aDart

            if (retDart != null)
                return retDart.cycle()
            else
                throw MalformedDCELException("Face.aDart is null")
        }
    }
}

class MalformedDCELException(msg: String) : Exception(msg)