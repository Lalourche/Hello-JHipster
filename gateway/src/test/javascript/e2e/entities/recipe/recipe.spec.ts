import { browser, element, by } from 'protractor';

import NavBarPage from './../../page-objects/navbar-page';
import SignInPage from './../../page-objects/signin-page';
import RecipeComponentsPage from './recipe.page-object';
import RecipeUpdatePage from './recipe-update.page-object';
import {
  waitUntilDisplayed,
  waitUntilAnyDisplayed,
  click,
  getRecordsCount,
  waitUntilHidden,
  waitUntilCount,
  isVisible,
} from '../../util/utils';
import path from 'path';

const expect = chai.expect;

describe('Recipe e2e test', () => {
  let navBarPage: NavBarPage;
  let signInPage: SignInPage;
  let recipeComponentsPage: RecipeComponentsPage;
  let recipeUpdatePage: RecipeUpdatePage;
  const username = process.env.E2E_USERNAME ?? 'admin';
  const password = process.env.E2E_PASSWORD ?? 'admin';

  before(async () => {
    await browser.get('/');
    navBarPage = new NavBarPage();
    signInPage = await navBarPage.getSignInPage();
    await signInPage.waitUntilDisplayed();
    await signInPage.username.sendKeys(username);
    await signInPage.password.sendKeys(password);
    await signInPage.loginButton.click();
    await signInPage.waitUntilHidden();
    await waitUntilDisplayed(navBarPage.entityMenu);
    await waitUntilDisplayed(navBarPage.adminMenu);
    await waitUntilDisplayed(navBarPage.accountMenu);
  });

  beforeEach(async () => {
    await browser.get('/');
    await waitUntilDisplayed(navBarPage.entityMenu);
    recipeComponentsPage = new RecipeComponentsPage();
    recipeComponentsPage = await recipeComponentsPage.goToPage(navBarPage);
  });

  it('should load Recipes', async () => {
    expect(await recipeComponentsPage.title.getText()).to.match(/Recipes/);
    expect(await recipeComponentsPage.createButton.isEnabled()).to.be.true;
  });

  it('should create and delete Recipes', async () => {
    const beforeRecordsCount = (await isVisible(recipeComponentsPage.noRecords)) ? 0 : await getRecordsCount(recipeComponentsPage.table);
    recipeUpdatePage = await recipeComponentsPage.goToCreateRecipe();
    await recipeUpdatePage.enterData();

    expect(await recipeComponentsPage.createButton.isEnabled()).to.be.true;
    await waitUntilDisplayed(recipeComponentsPage.table);
    await waitUntilCount(recipeComponentsPage.records, beforeRecordsCount + 1);
    expect(await recipeComponentsPage.records.count()).to.eq(beforeRecordsCount + 1);

    await recipeComponentsPage.deleteRecipe();
    if (beforeRecordsCount !== 0) {
      await waitUntilCount(recipeComponentsPage.records, beforeRecordsCount);
      expect(await recipeComponentsPage.records.count()).to.eq(beforeRecordsCount);
    } else {
      await waitUntilDisplayed(recipeComponentsPage.noRecords);
    }
  });

  after(async () => {
    await navBarPage.autoSignOut();
  });
});
