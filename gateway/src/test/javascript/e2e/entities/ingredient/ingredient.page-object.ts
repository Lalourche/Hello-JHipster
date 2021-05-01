import { element, by, ElementFinder, ElementArrayFinder } from 'protractor';

import { waitUntilAnyDisplayed, waitUntilDisplayed, click, waitUntilHidden, isVisible } from '../../util/utils';

import NavBarPage from './../../page-objects/navbar-page';

import IngredientUpdatePage from './ingredient-update.page-object';

const expect = chai.expect;
export class IngredientDeleteDialog {
  deleteModal = element(by.className('modal'));
  private dialogTitle: ElementFinder = element(by.id('gatewayApp.ingredient.delete.question'));
  private confirmButton = element(by.id('jhi-confirm-delete-ingredient'));

  getDialogTitle() {
    return this.dialogTitle;
  }

  async clickOnConfirmButton() {
    await this.confirmButton.click();
  }
}

export default class IngredientComponentsPage {
  createButton: ElementFinder = element(by.id('jh-create-entity'));
  deleteButtons = element.all(by.css('div table .btn-danger'));
  title: ElementFinder = element(by.id('ingredient-heading'));
  noRecords: ElementFinder = element(by.css('#app-view-container .table-responsive div.alert.alert-warning'));
  table: ElementFinder = element(by.css('#app-view-container div.table-responsive > table'));

  records: ElementArrayFinder = this.table.all(by.css('tbody tr'));

  getDetailsButton(record: ElementFinder) {
    return record.element(by.css('a.btn.btn-info.btn-sm'));
  }

  getEditButton(record: ElementFinder) {
    return record.element(by.css('a.btn.btn-primary.btn-sm'));
  }

  getDeleteButton(record: ElementFinder) {
    return record.element(by.css('a.btn.btn-danger.btn-sm'));
  }

  async goToPage(navBarPage: NavBarPage) {
    await navBarPage.getEntityPage('ingredient');
    await waitUntilAnyDisplayed([this.noRecords, this.table]);
    return this;
  }

  async goToCreateIngredient() {
    await this.createButton.click();
    return new IngredientUpdatePage();
  }

  async deleteIngredient() {
    const deleteButton = this.getDeleteButton(this.records.last());
    await click(deleteButton);

    const ingredientDeleteDialog = new IngredientDeleteDialog();
    await waitUntilDisplayed(ingredientDeleteDialog.deleteModal);
    expect(await ingredientDeleteDialog.getDialogTitle().getAttribute('id')).to.match(/gatewayApp.ingredient.delete.question/);
    await ingredientDeleteDialog.clickOnConfirmButton();

    await waitUntilHidden(ingredientDeleteDialog.deleteModal);

    expect(await isVisible(ingredientDeleteDialog.deleteModal)).to.be.false;
  }
}
