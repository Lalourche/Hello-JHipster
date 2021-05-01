import { IRecipe } from 'app/shared/model/recipe.model';

export interface IStep {
  id?: number;
  action?: string;
  recipes?: IRecipe[];
}

export const defaultValue: Readonly<IStep> = {};
