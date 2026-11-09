import { useState } from "react";
import Icon from "./Icon";
import { assetUrl } from "../../services/Api";
import { productPicture } from "../../utilis/categoryImages";
import "./ProductImage.css";

interface ProductImageProps {
  name: string;
  /** Picture uploaded by an admin, e.g. /uploads/products/ab12.jpg */
  imageUrl?: string | null;
  /** "card" for the shop grid, "big" for the product page, "thumb" for small table rows. */
  size?: "card" | "big" | "thumb";
  /** Describe the picture when it adds information (product page); leave empty when the name is shown next to it. */
  alt?: string;
}

const ICON_SIZE = { card: 44, big: 80, thumb: 20 };
const CLASS = { card: "product-art", big: "product-art big", thumb: "adm-thumb" };

/**
 * Shows the best picture we have for a product:
 * 1. the picture an admin uploaded, 2. the bundled picture matched by name, 3. a bag icon.
 * If a picture fails to load, it falls back to the next option instead of showing a broken image.
 */
function ProductImage({ name, imageUrl, size = "card", alt = "" }: ProductImageProps) {
  const sources = [assetUrl(imageUrl), productPicture(name)].filter((src): src is string => Boolean(src));
  const [failed, setFailed] = useState(0);
  const src = sources[failed];

  if (!src) {
    return (
      <div className={CLASS[size]} aria-hidden="true">
        <Icon name="bag" size={ICON_SIZE[size]} />
      </div>
    );
  }

  return (
    <img
      className={`${CLASS[size]} product-img`}
      src={src}
      alt={alt}
      loading="lazy"
      onError={() => setFailed((count) => count + 1)}
    />
  );
}

export default ProductImage;
