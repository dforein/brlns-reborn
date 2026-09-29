package org.brlnsreb.utils;

import java.util.Objects;

import org.powernukkitx.block.Block;
import org.powernukkitx.level.Level;
import org.powernukkitx.math.Vector3;

public class Vect {

        public static final int X = 0;
        public static final int Y = 1;
        public static final int Z = 2;
        
        public double x;
        public double y;
        public double z;

        public Vect() {
            this(0.0, 0.0, 0.0);
        }

        public Vect(Vector3 v) {
            this(v.x, v.y, v.z);
        }

        public Vect(double x, double y, double z) {
            set(x, y, z);
        }

        public Vect set(double coordValue, int coord) {
            switch (coord) {
                case X -> this.x = coordValue;
                case Y -> this.y = coordValue;
                case Z -> this.z = coordValue;
            }
            return this;
        }

        public Vect set(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
            return this;
        }

        public Vect set(Vector3 v) {
            this.x = v.x;
            this.y = v.y;
            this.z = v.z;
            return this;
        }

        public Vect set(Vect v) {
            this.x = v.x;
            this.y = v.y;
            this.z = v.z;
            return this;
        }

        public Vect add(double dx) { return add(dx, X); }
        public Vect add(double dcoord, int coord) {
            switch (coord) {
                case X -> this.x += dcoord;
                case Y -> this.y += dcoord;
                case Z -> this.z += dcoord;
            }
            return this;
        }

        public Vect add(double dx, double dy) { return add(dx, dy, 0); }
        public Vect add(double dx, double dy, double dz) {
            this.x += dx;
            this.y += dy;
            this.z += dz;
            return this;
        }

        public int xFloor() {return (int) this.x;}
        public int yFloor() {return (int) this.y;}
        public int zFloor() {return (int) this.z;}

        public Block getBlock(Level level) {
            return level.getBlock((int) x, (int) y, (int) z);
        }

        public Vector3 getNewVector3() {
            return new Vector3(this.x, this.y, this.z);
        }

        public Vect clone() {
            return new Vect(this.x, this.y, this.z);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Vect v = (Vect) o;
            return Double.compare(v.x, this.x) == 0 &&
                Double.compare(v.y, this.y) == 0 &&
                Double.compare(v.z, this.z) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.x, this.y, this.z);
        }

    }