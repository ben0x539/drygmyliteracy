package org._1d6.drygmyliteracy;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WritableBookContent;

import java.util.ArrayList;
import java.util.List;

public class BookWriter {
    private final List<Filterable<String>> pages;
    private List<String> newPage;

    public BookWriter(ItemStack book) {
        WritableBookContent content = book.get(DataComponents.WRITABLE_BOOK_CONTENT);
        if (content != null) {
            pages = new ArrayList<>(content.pages());
        } else {
            pages = new ArrayList<>();
        }
        newPage = null;
    }

    private boolean maybePrepareNewPage() {
        if (pages.size() >= 100) {
            return false;
        }

        if (newPage != null && !newPage.isEmpty()) {
            pages.add(Filterable.passThrough(String.join("\n", newPage)));
        }

        newPage = new ArrayList<>();
        return true;
    }

    public boolean writeLine(String line) {
        if (newPage == null || newPage.size() >= 14) {
            if (!maybePrepareNewPage())
                return false;
        }

        newPage.add(line);
        return true;
    }

    public void update(ItemStack book) {
        maybePrepareNewPage();
        // todo do we need to stop mutating pages after this? like it won't come up but
        book.set(DataComponents.WRITABLE_BOOK_CONTENT, new WritableBookContent(pages));
    }
}
