package org.max.budgetcontrol;

import android.view.MenuItem;

public class WidgetParamsStateListener {
    private final MenuItem saveMenu;
    private boolean categoriesComplete;
    private boolean amountLimitComplete;
    private boolean titleComplete;

    WidgetParamsStateListener(MenuItem saveMenu) {
        this.saveMenu = saveMenu;
    }

    void changeMenuState() {
        saveMenu.setEnabled(isCategoriesComplete() && isTitleComplete() && isAmountLimitComplete());
    }

    private boolean isAmountLimitComplete() {
        return amountLimitComplete;
    }

    private boolean isTitleComplete() {
        return titleComplete;
    }

    public boolean isCategoriesComplete() {
        return categoriesComplete;
    }

    public void setCategoriesComplete(boolean categoriesComplete) {
        this.categoriesComplete = categoriesComplete;
        changeMenuState();
    }

    public void setAmountLimitComplete(boolean amountLimitComplete) {
        this.amountLimitComplete = amountLimitComplete;
        changeMenuState();
    }

    public void setTitleComplete(boolean titleComplete) {
        this.titleComplete = titleComplete;
        changeMenuState();
    }
}
