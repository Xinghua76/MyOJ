/* generated using openapi-typescript-codegen -- do no edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { OrderItem } from './OrderItem';
import type { SolutionCommentVO } from './SolutionCommentVO';

export type Page_SolutionCommentVO_ = {
    countId?: string;
    current?: number;
    maxLimit?: number;
    optimizeCountSql?: boolean;
    orders?: Array<OrderItem>;
    pages?: number;
    records?: Array<SolutionCommentVO>;
    searchCount?: boolean;
    size?: number;
    total?: number;
};