/* generated using openapi-typescript-codegen -- do no edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { ContestRankVO } from './ContestRankVO';
import type { OrderItem } from './OrderItem';

export type Page_ContestRankVO_ = {
    countId?: string;
    current?: number;
    maxLimit?: number;
    optimizeCountSql?: boolean;
    orders?: Array<OrderItem>;
    pages?: number;
    records?: Array<ContestRankVO>;
    searchCount?: boolean;
    size?: number;
    total?: number;
};
