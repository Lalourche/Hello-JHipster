import { browser, element, by } from 'protractor';

import NavBarPage from './../../page-objects/navbar-page';
import SignInPage from './../../page-objects/signin-page';
import TechniqueComponentsPage from './technique.page-object';
import TechniqueUpdatePage from './technique-update.page-object';
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

describe('Technique e2e test', () => {
  let navBarPage: NavBarPage;
  let signInPage: SignInPage;
  let techniqueComponentsPage: TechniqueComponentsPage;
  let techniqueUpdatePage: TechniqueUpdatePage;
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
    techniqueComponentsPage = new TechniqueComponentsPage();
    techniqueComponentsPage = await techniqueComponentsPage.goToPage(navBarPage);
  });

  it('should load Techniques', async () => {
    expect(await techniqueComponentsPage.title.getText()).to.match(/Techniques/);
    expect(await techniqueComponentsPage.createButton.isEnabled()).to.be.true;
  });

  it('should create and delete Techniques', async () => {
    const beforeRecordsCount = (await isVisible(techniqueComponentsPage.noRecords))
      ? 0
      : await getRecordsCount(techniqueComponentsPage.table);
    techniqueUpdatePage = await techniqueComponentsPage.goToCreateTechnique();
    await techniqueUpdatePage.enterData();

    expect(await techniqueComponentsPage.createButton.isEnabled()).to.be.true;
    await waitUntilDisplayed(techniqueComponentsPage.table);
    await waitUntilCount(techniqueComponentsPage.records, beforeRecordsCount + 1);
    expect(await techniqueComponentsPage.records.count()).to.eq(beforeRecordsCount + 1);

    await techniqueComponentsPage.deleteTechnique();
    if (beforeRecordsCount !== 0) {
      await waitUntilCount(techniqueComponentsPage.records, beforeRecordsCount);
      expect(await techniqueComponentsPage.records.count()).to.eq(beforeRecordsCount);
    } else {
      await waitUntilDisplayed(techniqueComponentsPage.noRecords);
    }
  });

  after(async () => {
    await navBarPage.autoSignOut();
  });
});
