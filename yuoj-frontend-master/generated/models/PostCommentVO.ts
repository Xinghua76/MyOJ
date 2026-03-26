/* generated using openapi-typescript-codegen -- do no edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { UserVO } from './UserVO';

export type PostCommentVO = {
    content?: string;
    createTime?: string;
    id?: number;
    thumbNum?: number;
    parentId?: number;
    postId?: number;
    status?: number;
    updateTime?: string;
    user?: UserVO;
    userId?: number;
    hasThumb?: boolean;
};