import { element, by, ElementFinder, ElementArrayFinder } from 'protractor';

import { waitUntilAnyDisplayed, waitUntilDisplayed, click, waitUntilHidden, isVisible } from '../../util/utils';

import NavBarPage from './../../page-objects/navbar-page';

import StepUpdatePage from './step-update.page-object';

const expect = chai.expect;
export class StepDeleteDialog {
  deleteModal = element(by.className('modal'));
  private dialogTitle: ElementFinder = element(by.id('gatewayApp.step.delete.question'));
  private confirmButton = element(by.id('jhi-confirm-delete-step'));

  getDialogTitle() {
    return this.dialogTitle;
  }

  async clickOnConfirmButton() {
    await this.confirmButton.click();
  }
}

export default class StepComponentsPage {
  createButton: ElementFinder = element(by.id('jh-create-entity'));
  deleteButtons = element.all(by.css('div table .btn-danger'));
  title: ElementFinder = element(by.id('step-heading'));
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
    await navBarPage.getEntityPage('step');
    await waitUntilAnyDisplayed([this.noRecords, this.table]);
    return this;
  }

  async goToCreateStep() {
    await this.createButton.click();
    return new StepUpdatePage();
  }

  async deleteStep() {
    const deleteButton = this.getDeleteButton(this.records.last());
    await click(deleteButton);

    const stepDeleteDialog = new StepDeleteDialog();
    await waitUntilDisplayed(stepDeleteDialog.deleteModal);
    expect(await stepDeleteDialog.getDialogTitle().getAttribute('id')).to.match(/gatewayApp.step.delete.question/);
    await stepDeleteDialog.clickOnConfirmButton();

    await waitUntilHidden(stepDeleteDialog.deleteModal);

    expect(await isVisible(stepDeleteDialog.deleteModal)).to.be.false;
  }
}
