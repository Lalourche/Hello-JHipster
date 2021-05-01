import { IIngredient } from 'app/shared/model/ingredient.model';
import { IStep } from 'app/shared/model/step.model';
import { Cooking } from 'app/shared/model/enumerations/cooking.model';

export interface IRecipe {
  id?: number;
  name?: string;
  cooking?: Cooking;
  cookingTime?: number | null;
  pictureContentType?: string | null;
  picture?: string | null;
  ingredients?: IIngredient[] | null;
  steps?: IStep[] | null;
}

export const defaultValue: Readonly<IRecipe> = {};
