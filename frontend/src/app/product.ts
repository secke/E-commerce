import { Image } from "./image"

export interface Product {
    description: string;
    id: string;
    images: Image[];
    name: string;
    price: number;
    quantity: number;
    userId: string;
}
