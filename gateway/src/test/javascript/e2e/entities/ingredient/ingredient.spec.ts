import { browser, element, by } from 'protractor';

import NavBarPage from './../../page-objects/navbar-page';
import SignInPage from './../../page-objects/signin-page';
import IngredientComponentsPage from './ingredient.page-object';
import IngredientUpdatePage from './ingredient-update.page-object';
import {
  waitUntilDisplayed,
  waitUntilAnyDisplayed,
  click,
  getRecordsCount,
  waitUntilHidden,
  waitUntilCount,
  isVisible,
} from '../../util/utils';

const expect = chai.expect;

describe('Ingredient e2e test', () => {
  let navBarPage: NavBarPage;
  let signInPage: SignInPage;
  let ingredientComponentsPage: IngredientComponentsPage;
  let ingredientUpdatePage: IngredientUpdatePage;
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
    ingredientComponentsPage = new IngredientComponentsPage();
    ingredientComponentsPage = await ingredientComponentsPage.goToPage(navBarPage);
  });

  it('should load Ingredients', async () => {
    expect(await ingredientComponentsPage.title.getText()).to.match(/Ingredients/);
    expect(await ingredientComponentsPage.createButton.isEnabled()).to.be.true;
  });

  /* it('should create and delete Ingredients', async () => {
        const beforeRecordsCount = await isVisible(ingredientComponentsPage.noRecords) ? 0 : await getRecordsCount(ingredientComponentsPage.table);
        ingredientUpdatePage = await ingredientComponentsPage.goToCreateIngredient();
        await ingredientUpdatePage.enterData();

        expect(await ingredientComponentsPage.createButton.isEnabled()).to.be.true;
        await waitUntilDisplayed(ingredientComponentsPage.table);
        await waitUntilCount(ingredientComponentsPage.records, beforeRecordsCount + 1);
        expect(await ingredientComponentsPage.records.count()).to.eq(beforeRecordsCount + 1);

        await ingredientComponentsPage.deleteIngredient();
        if(beforeRecordsCount !== 0) {
          await waitUntilCount(ingredientComponentsPage.records, beforeRecordsCount);
          expect(await ingredientComponentsPage.records.count()).to.eq(beforeRecordsCount);
        } else {
          await waitUntilDisplayed(ingredientComponentsPage.noRecords);
        }
    }); */

  after(async () => {
    await navBarPage.autoSignOut();
  });
});
