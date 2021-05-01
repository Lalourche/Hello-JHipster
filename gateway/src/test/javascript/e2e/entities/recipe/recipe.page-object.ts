import { element, by, ElementFinder, ElementArrayFinder } from 'protractor';

import { waitUntilAnyDisplayed, waitUntilDisplayed, click, waitUntilHidden, isVisible } from '../../util/utils';

import NavBarPage from './../../page-objects/navbar-page';

import RecipeUpdatePage from './recipe-update.page-object';

const expect = chai.expect;
export class RecipeDeleteDialog {
  deleteModal = element(by.className('modal'));
  private dialogTitle: ElementFinder = element(by.id('gatewayApp.recipe.delete.question'));
  private confirmButton = element(by.id('jhi-confirm-delete-recipe'));

  getDialogTitle() {
    return this.dialogTitle;
  }

  async clickOnConfirmButton() {
    await this.confirmButton.click();
  }
}

export default class RecipeComponentsPage {
  createButton: ElementFinder = element(by.id('jh-create-entity'));
  deleteButtons = element.all(by.css('div table .btn-danger'));
  title: ElementFinder = element(by.id('recipe-heading'));
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
    await navBarPage.getEntityPage('recipe');
    await waitUntilAnyDisplayed([this.noRecords, this.table]);
    return this;
  }

  async goToCreateRecipe() {
    await this.createButton.click();
    return new RecipeUpdatePage();
  }

  async deleteRecipe() {
    const deleteButton = this.getDeleteButton(this.records.last());
    await click(deleteButton);

    const recipeDeleteDialog = new RecipeDeleteDialog();
    await waitUntilDisplayed(recipeDeleteDialog.deleteModal);
    expect(await recipeDeleteDialog.getDialogTitle().getAttribute('id')).to.match(/gatewayApp.recipe.delete.question/);
    await recipeDeleteDialog.clickOnConfirmButton();

    await waitUntilHidden(recipeDeleteDialog.deleteModal);

    expect(await isVisible(recipeDeleteDialog.deleteModal)).to.be.false;
  }
}
