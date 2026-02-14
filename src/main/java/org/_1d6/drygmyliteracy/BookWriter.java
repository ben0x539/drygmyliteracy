package org._1d6.drygmyliteracy;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WritableBookContent;

import java.util.ArrayList;
import java.util.List;

public class BookWriter {
    // I don't know what the fuck we're supposed to be doing with filterable
    // strings, so, uh, let's just leave existing pages completely untouched
    // and track a new, in-progress page using regular strings and then
    // send that into a passthrough filterable?
    //
    // We maintain the invariant of pages having 100 or less elements, and
    // additionally keep newPage null if it is at 100 elements. newPage of
    // course has to be under 14 elements.
    //
    // TODO: the hard part is going to be keeping each line in newPage from
    // being long enough to need to be wrapped, because then we lose track
    // of how many real, wrapped lines newPage is going to take up, and we
    // will easily overrun the end of the page, visually.
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
        while (!line.isEmpty()) {
            // TODO: hard-wrapping at 19 looks pretty bad, but we don't have
            // font metrics on the server side. So unless we want to sort of
            // measure the default font by hand and calculate the exact
            // string with ourselves there isn't really a good thing to do
            // here.
            // TODO: Don't break in the middle of words or before punctuation,
            // I guess.
            int n = Math.min(19, line.length());
            String prefix = line.substring(0, n);
            line = line.substring(n);

            if (newPage == null || newPage.size() >= 14) {
                if (!maybePrepareNewPage())
                    return false;
            }

            newPage.add(prefix);
        }
        return true;
    }

    public void update(ItemStack book) {
        maybePrepareNewPage();
        // todo do we need to stop mutating pages after this? like it won't come up but
        book.set(DataComponents.WRITABLE_BOOK_CONTENT, new WritableBookContent(pages));
    }
}
