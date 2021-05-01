import { element, by, ElementFinder } from 'protractor';
import { waitUntilDisplayed, waitUntilHidden, isVisible } from '../../util/utils';

import path from 'path';

const expect = chai.expect;

const fileToUpload = '../../../../../../src/main/webapp/content/images/logo-jhipster.png';
const absolutePath = path.resolve(__dirname, fileToUpload);
export default class RecipeUpdatePage {
  pageTitle: ElementFinder = element(by.id('gatewayApp.recipe.home.createOrEditLabel'));
  saveButton: ElementFinder = element(by.id('save-entity'));
  cancelButton: ElementFinder = element(by.id('cancel-save'));
  nameInput: ElementFinder = element(by.css('input#recipe-name'));
  cookingSelect: ElementFinder = element(by.css('select#recipe-cooking'));
  cookingTimeInput: ElementFinder = element(by.css('input#recipe-cookingTime'));
  pictureInput: ElementFinder = element(by.css('input#file_picture'));
  ingredientsSelect: ElementFinder = element(by.css('select#recipe-ingredients'));
  stepsSelect: ElementFinder = element(by.css('select#recipe-steps'));

  getPageTitle() {
    return this.pageTitle;
  }

  async setNameInput(name) {
    await this.nameInput.sendKeys(name);
  }

  async getNameInput() {
    return this.nameInput.getAttribute('value');
  }

  async setCookingSelect(cooking) {
    await this.cookingSelect.sendKeys(cooking);
  }

  async getCookingSelect() {
    return this.cookingSelect.element(by.css('option:checked')).getText();
  }

  async cookingSelectLastOption() {
    await this.cookingSelect.all(by.tagName('option')).last().click();
  }
  async setCookingTimeInput(cookingTime) {
    await this.cookingTimeInput.sendKeys(cookingTime);
  }

  async getCookingTimeInput() {
    return this.cookingTimeInput.getAttribute('value');
  }

  async setPictureInput(picture) {
    await this.pictureInput.sendKeys(picture);
  }

  async getPictureInput() {
    return this.pictureInput.getAttribute('value');
  }

  async ingredientsSelectLastOption() {
    await this.ingredientsSelect.all(by.tagName('option')).last().click();
  }

  async ingredientsSelectOption(option) {
    await this.ingredientsSelect.sendKeys(option);
  }

  getIngredientsSelect() {
    return this.ingredientsSelect;
  }

  async getIngredientsSelectedOption() {
    return this.ingredientsSelect.element(by.css('option:checked')).getText();
  }

  async stepsSelectLastOption() {
    await this.stepsSelect.all(by.tagName('option')).last().click();
  }

  async stepsSelectOption(option) {
    await this.stepsSelect.sendKeys(option);
  }

  getStepsSelect() {
    return this.stepsSelect;
  }

  async getStepsSelectedOption() {
    return this.stepsSelect.element(by.css('option:checked')).getText();
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
    await this.setNameInput('name');
    expect(await this.getNameInput()).to.match(/name/);
    await waitUntilDisplayed(this.saveButton);
    await this.cookingSelectLastOption();
    await waitUntilDisplayed(this.saveButton);
    await this.setCookingTimeInput('5');
    expect(await this.getCookingTimeInput()).to.eq('5');
    await waitUntilDisplayed(this.saveButton);
    await this.setPictureInput(absolutePath);
    // this.ingredientsSelectLastOption();
    // this.stepsSelectLastOption();
    await this.save();
    await waitUntilHidden(this.saveButton);
    expect(await isVisible(this.saveButton)).to.be.false;
  }
}
