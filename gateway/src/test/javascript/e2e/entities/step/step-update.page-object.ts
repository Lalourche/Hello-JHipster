import { element, by, ElementFinder } from 'protractor';
import { waitUntilDisplayed, waitUntilHidden, isVisible } from '../../util/utils';

const expect = chai.expect;

export default class StepUpdatePage {
  pageTitle: ElementFinder = element(by.id('gatewayApp.step.home.createOrEditLabel'));
  saveButton: ElementFinder = element(by.id('save-entity'));
  cancelButton: ElementFinder = element(by.id('cancel-save'));
  actionInput: ElementFinder = element(by.css('input#step-action'));

  getPageTitle() {
    return this.pageTitle;
  }

  async setActionInput(action) {
    await this.actionInput.sendKeys(action);
  }

  async getActionInput() {
    return this.actionInput.getAttribute('value');
  }

  async save() {
    await this.saveButton.click();
  }

  async cancel() {
    await this.cancelButton.click();
  }

  getSaveButton() {
    return this.saveButton;
  }

  async enterData() {
    await waitUntilDisplayed(this.saveButton);
    await this.setActionInput('action');
    expect(await this.getActionInput()).to.match(/action/);
    await this.save();
    await waitUntilHidden(this.saveButton);
    expect(await isVisible(this.saveButton)).to.be.false;
  }
}
