// Pictures for the shop's products, bundled with the app (src/assets/categories/).
// Each picture belongs to one product, so it is matched by the product's NAME.
// The most specific rules come first ("fabric softener sachet" before "fabric softener").
// A picture an admin uploads on the dashboard always wins over these (see ProductImage.tsx).
import clothesPegs from "../assets/categories/clothes-pegs.webp";
import colourCatcher from "../assets/categories/colour-catcher.webp";
import detergentSachet from "../assets/categories/detergent-sachet.webp";
import dryerSheets from "../assets/categories/dryer-sheets.webp";
import fabricSoftener from "../assets/categories/fabric-softener.webp";
import laundryBasket from "../assets/categories/laundry-basket.webp";
import laundryLiquid from "../assets/categories/laundry-liquid.webp";
import meshLaundryBag from "../assets/categories/mesh-laundry-bag.webp";
import softenerSachet from "../assets/categories/softener-sachet.webp";
import stainRemoverSpray from "../assets/categories/stain-remover-spray.webp";
import washingPowder from "../assets/categories/washing-powder.webp";
import shopping from "../assets/categories/shopping.webp";

/** Every word in `words` must appear in the product name for the picture to match. */
const RULES: { words: string[]; image: string }[] = [
  { words: ["softener", "sachet"], image: softenerSachet },
  { words: ["detergent", "sachet"], image: detergentSachet },
  { words: ["single-wash"], image: detergentSachet },
  { words: ["fabric softener"], image: fabricSoftener },
  { words: ["laundry liquid"], image: laundryLiquid },
  { words: ["washing powder"], image: washingPowder },
  { words: ["stain", "spray"], image: stainRemoverSpray },
  { words: ["colour catcher"], image: colourCatcher },
  { words: ["color catcher"], image: colourCatcher },
  { words: ["dryer sheet"], image: dryerSheets },
  { words: ["mesh"], image: meshLaundryBag },
  { words: ["peg"], image: clothesPegs },
  { words: ["basket"], image: laundryBasket },
  {words: ["detergent"], image: shopping }
];

/** The bundled picture for a product, or undefined when there isn't one (then the bag icon shows). */
export function productPicture(productName?: string | null): string | undefined {
  if (!productName) return undefined;
  const name = productName.toLowerCase();
  return RULES.find((rule) => rule.words.every((word) => name.includes(word)))?.image;
}
