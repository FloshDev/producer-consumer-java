package producerconsumer.model;

import java.util.Objects;

public final class Item {

    public static final Item POISON = new Item(-1, -1);

    private final int id;
    private final int producerId;

    public Item(int id, int producerId) {
        this.id = id;
        this.producerId = producerId;
    }

    public int getId() {
        return id;
    }

    public int getProducerId() {
        return producerId;
    }

    public boolean isPoison() {
        return this == POISON;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Item)) {
            return false;
        }
        Item other = (Item) o;
        return id == other.id && producerId == other.producerId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, producerId);
    }

    @Override
    public String toString() {
        return "Item[id=" + id + ", producerId=" + producerId + "]";
    }
}
