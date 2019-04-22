package cn.qiuxiang.react.amap3d.dt

import android.graphics.Color

/**
 * Created by lee on 2019/2/26.
 */
object ObjRender {
    val Cell_SIZE: Int = 72
    val CELL_COLOR= CellStyle("LTE", Color.parseColor("#0F83E6") , Color.BLACK)
    val CELL_LTE_OUT_PATH = CellPath(200, 200, "M124.223,52.462c0,0-24.223-13.962-53,0 l26.5,95.624L124.223,52.462z")
    val CELL_GSM_INNER_PATH=CellPath(1024,1024,"M512 56.32c-233.472 0-422.707 189.235-422.707 422.707C89.293 712.5 278.528 901.734 512 901.734S934.707 712.5 934.707 479.027C934.707 245.555 745.472 56.32 512 56.32z m0 807.526c-212.582 0-384.82-172.236-384.82-384.819S299.419 94.208 512 94.208s384.82 172.237 384.82 384.82S724.581 863.845 512 863.845z M272.18 479.027a117.1 117.1 0 1 0 479.64 0 117.1 117.1 0 1 0-479.64 0z")


    //region 未使用部分
    val CELL_STYLE_LTE: CellStyle = CellStyle("LTE", Color.argb(150,0,0,200), Color.BLACK)
    val CELL_STYLE_GSM: CellStyle = CellStyle("GSM", Color.GREEN, Color.BLACK)

    val CELL_OUT_PATH_45 = CellPath(512, 512, "M256 507L63.90 43.2C156.146 5 355.854 5 448.1 43.2z")
    val CELL_OUT_PATH_30 = CellPath(512, 512, "M256 507L126 22.105C189.9104462 5 322.0895538 5 386 22.105z")
    val CELL_INNER_PATH = CellPath(512, 512, "M256 228.719c-22.879 0-41.597 18.529-41.597 41.18 0 22.652 18.718 41.182 41.597 41.182 22.878 0 41.597-18.529 41.597-41.182 0-22.651-18.719-41.18-41.597-41.18zm124.8 41.179c0-67.946-56.163-123.539-124.8-123.539s-124.8 55.593-124.8 123.539c0 45.303 24.961 85.447 62.396 107.072l20.807-36.032c-24.972-14.417-41.604-40.153-41.604-71.04 0-45.295 37.433-82.358 83.201-82.358 45.771 0 83.201 37.063 83.201 82.358 0 30.887-16.633 56.623-41.604 71.04l20.807 36.032c37.433-21.624 62.396-61.769 62.396-107.072zM256 64C141.597 64 48 156.654 48 269.898 48 346.085 89.592 411.968 152 448l20.799-36.032c-49.919-28.824-83.207-81.324-83.207-142.069 0-90.593 74.891-164.718 166.408-164.718 91.517 0 166.406 74.125 166.406 164.718 0 60.745-33.284 114.271-83.205 142.069L360 448c62.406-36.032 104-101.915 104-178.102C464 156.654 370.403 64 256 64z")
    val CELL_INNER_PATH_ACCESS = CellPath(24, 24, "M4.93,4.93C3.12,6.74 2,9.24 2,12C2,14.76 3.12,17.26 4.93,19.07L6.34,17.66C4.89,16.22 4,14.22 4,12C4,9.79 4.89,7.78 6.34,6.34L4.93,4.93M19.07,4.93L17.66,6.34C19.11,7.78 20,9.79 20,12C20,14.22 19.11,16.22 17.66,17.66L19.07,19.07C20.88,17.26 22,14.76 22,12C22,9.24 20.88,6.74 19.07,4.93M7.76,7.76C6.67,8.85 6,10.35 6,12C6,13.65 6.67,15.15 7.76,16.24L9.17,14.83C8.45,14.11 8,13.11 8,12C8,10.89 8.45,9.89 9.17,9.17L7.76,7.76M16.24,7.76L14.83,9.17C15.55,9.89 16,10.89 16,12C16,13.11 15.55,14.11 14.83,14.83L16.24,16.24C17.33,15.15 18,13.65 18,12C18,10.35 17.33,8.85 16.24,7.76M12,10C10.9,10 10,10.9 10,12C10,13.1 10.9,14 12,14C13.1,14 14,13.1 14,12C14,10.9 13.1,10 12,10Z")
    val CELL_INNER_PATH_ADJUST = CellPath(24, 24, "M12,2C6.48,2 2,6.48 2,12C2,17.52 6.48,22 12,22C17.52,22 22,17.52 22,12C22,6.48 17.52,2 12,2M12,20C7.59,20 4,16.41 4,12C4,7.59 7.59,4 12,4C16.41,4 20,7.59 20,12C20,16.41 16.41,20 12,20M15,12C15,13.66 13.66,15 12,15C10.34,15 9,13.66 9,12C9,10.34 10.34,9 12,9C13.66,9 15,10.34 15,12Z")
    val ORDER_INNER_PATH = CellPath(1024, 1024, "M885.91883 788.429584H547.932985L119.439534 981.822442l74.083711-193.392858h-55.421189C85.129519 788.429584 42.187503 745.487568 42.187503 692.515031V138.091115C42.187503 85.118578 85.129519 42.176563 138.102056 42.176563H885.917835C938.891367 42.176563 981.833383 85.118578 981.833383 138.091115v554.423916c0 52.972536-42.942016 95.914552-95.914553 95.914553z")
    val ORDER_OUT_PATH = CellPath(1024, 1024, "M119.449488 1024a42.177558 42.177558 0 0 1-39.39545-57.265643l52.194143-136.251639c-73.438701-3.078734-132.237241-63.786427-132.23724-137.968682V138.091115c0-76.144164 61.946951-138.091115 138.091115-138.091115H885.917835c76.143169 0 138.091115 61.946951 138.091115 138.091115v554.422921c0 76.143169-61.947946 138.091115-138.091115 138.091115H557.009925L136.790135 1020.26431a42.115844 42.115844 0 0 1-17.340647 3.73569z m18.652568-939.646875c-29.631693 0-53.738985 24.107292-53.738985 53.738985v554.422921c0 29.631693 24.107292 53.737989 53.738985 53.73799h55.420194a42.174572 42.174572 0 0 1 39.385496 57.263652L195.477191 901.231882l335.106189-151.244167a42.172581 42.172581 0 0 1 17.350601-3.734694h337.985845c29.631693 0 53.737989-24.107292 53.737989-53.73799V138.091115c0-29.631693-24.107292-53.738985-53.737989-53.738985H138.102056zM659.16058 585.73881a19.859973 19.859973 0 0 1-14.65111-6.423236c-32.235627-35.011763-81.071333-55.092712-133.987131-55.092712-51.251511 0-99.131645 19.067645-131.364286 52.314585-7.65254 7.894419-20.256137 8.09051-28.150556 0.43598-7.894419-7.65254-8.089515-20.256137-0.43598-28.149561 39.688094-40.937305 97.986949-64.416507 159.949826-64.416508 64.014371 0 123.526604 24.762257 163.279398 67.939185 7.446495 8.08852 6.926902 20.682163-1.161617 28.130648a19.835088 19.835088 0 0 1-13.478544 5.261619zM368.179923 308.936454m-50.058041 0a50.058042 50.058042 0 1 0 100.116083 0 50.058042 50.058042 0 1 0-100.116083 0ZM655.840963 308.936454m-50.058042 0a50.058042 50.058042 0 1 0 100.116083 0 50.058042 50.058042 0 1 0-100.116083 0Z")
//endregion
}

data class CellStyle(var network: String, var color: Int, var strokeColor: Int, var alpha: Float = 1f)
data class CellPath(var width: Int, var height: Int, var pathData: String)