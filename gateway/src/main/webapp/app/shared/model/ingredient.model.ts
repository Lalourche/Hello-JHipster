import { IRecipe } from 'app/shared/model/recipe.model';

export interface IIngredient {
  id?: number;
  name?: string;
  recipes?: IRecipe[];
}

export const defaultValue: Readonly<IIngredient> = {};
