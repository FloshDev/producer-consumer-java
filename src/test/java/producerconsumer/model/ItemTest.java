package producerconsumer.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ItemTest {

    @Test
    void itemsWithSameValuesAreEqual() {
        assertEquals(new Item(1, 0), new Item(1, 0));
        assertNotEquals(new Item(1, 0), new Item(2, 0));
        assertNotEquals(new Item(1, 0), new Item(1, 1));
    }

    @Test
    void exposesIdAndProducerId() {
        Item item = new Item(7, 3);
        assertEquals(7, item.getId());
        assertEquals(3, item.getProducerId());
    }

    @Test
    void poisonIsRecognized() {
        assertTrue(Item.POISON.isPoison());
        assertFalse(new Item(0, 0).isPoison());
    }
}
