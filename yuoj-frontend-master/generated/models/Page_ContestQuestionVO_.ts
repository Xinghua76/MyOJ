/* generated using openapi-typescript-codegen -- do no edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { ContestQuestionVO } from './ContestQuestionVO';
import type { OrderItem } from './OrderItem';

export type Page_ContestQuestionVO_ = {
    countId?: string;
    current?: number;
    maxLimit?: number;
    optimizeCountSql?: boolean;
    orders?: Array<OrderItem>;
    pages?: number;
    records?: Array<ContestQuestionVO>;
    searchCount?: boolean;
    size?: number;
    total?: number;
};
