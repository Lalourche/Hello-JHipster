import { browser, element, by } from 'protractor';

import NavBarPage from './../../page-objects/navbar-page';
import SignInPage from './../../page-objects/signin-page';
import StepComponentsPage from './step.page-object';
import StepUpdatePage from './step-update.page-object';
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

describe('Step e2e test', () => {
  let navBarPage: NavBarPage;
  let signInPage: SignInPage;
  let stepComponentsPage: StepComponentsPage;
  let stepUpdatePage: StepUpdatePage;
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
    stepComponentsPage = new StepComponentsPage();
    stepComponentsPage = await stepComponentsPage.goToPage(navBarPage);
  });

  it('should load Steps', async () => {
    expect(await stepComponentsPage.title.getText()).to.match(/Steps/);
    expect(await stepComponentsPage.createButton.isEnabled()).to.be.true;
  });

  /* it('should create and delete Steps', async () => {
        const beforeRecordsCount = await isVisible(stepComponentsPage.noRecords) ? 0 : await getRecordsCount(stepComponentsPage.table);
        stepUpdatePage = await stepComponentsPage.goToCreateStep();
        await stepUpdatePage.enterData();

        expect(await stepComponentsPage.createButton.isEnabled()).to.be.true;
        await waitUntilDisplayed(stepComponentsPage.table);
        await waitUntilCount(stepComponentsPage.records, beforeRecordsCount + 1);
        expect(await stepComponentsPage.records.count()).to.eq(beforeRecordsCount + 1);

        await stepComponentsPage.deleteStep();
        if(beforeRecordsCount !== 0) {
          await waitUntilCount(stepComponentsPage.records, beforeRecordsCount);
          expect(await stepComponentsPage.records.count()).to.eq(beforeRecordsCount);
        } else {
          await waitUntilDisplayed(stepComponentsPage.noRecords);
        }
    }); */

  after(async () => {
    await navBarPage.autoSignOut();
  });
});
