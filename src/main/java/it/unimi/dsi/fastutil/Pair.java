package it.unimi.dsi.fastutil;

import java.io.Serializable;
import java.util.Objects;

public interface Pair<L, R> extends Serializable {
    L left();
    R right();

    default L key() {
        return left();
    }

    default R value() {
        return right();
    }

    static <L, R> Pair<L, R> of(L left, R right) {
        return new ImmutablePair<>(left, right);
    }

    class ImmutablePair<L, R> implements Pair<L, R> {
        private static final long serialVersionUID = 1L;
        private final L left;
        private final R right;

        public ImmutablePair(L left, R right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public L left() {
            return this.left;
        }

        @Override
        public R right() {
            return this.right;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Pair)) return false;
            Pair<?, ?> other = (Pair<?, ?>) obj;
            return Objects.equals(this.left, other.left()) && Objects.equals(this.right, other.right());
        }

        @Override
        public int hashCode() {
            return (this.left == null ? 0 : this.left.hashCode()) * 31 + (this.right == null ? 0 : this.right.hashCode());
        }

        @Override
        public String toString() {
            return "<" + this.left + "," + this.right + ">";
        }
    }
}
