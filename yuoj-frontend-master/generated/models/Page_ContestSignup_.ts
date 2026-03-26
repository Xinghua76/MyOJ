/* generated using openapi-typescript-codegen -- do no edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { ContestSignup } from './ContestSignup';
import type { OrderItem } from './OrderItem';

export type Page_ContestSignup_ = {
    countId?: string;
    current?: number;
    maxLimit?: number;
    optimizeCountSql?: boolean;
    orders?: Array<OrderItem>;
    pages?: number;
    records?: Array<ContestSignup>;
    searchCount?: boolean;
    size?: number;
    total?: number;
};
